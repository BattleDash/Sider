package net.battledash.sider.messages.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.battledash.sider.messages.SiderMessage;
import net.battledash.sider.messages.SiderMessageChannel;
import net.battledash.sider.stream.SiderInputStream;
import net.battledash.sider.stream.SiderOutputStream;
import net.battledash.sider.utils.HashUtil;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.SafeEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SiderMessageChannelImpl extends BinaryJedisPubSub implements SiderMessageChannel {

    private static final Logger logger = Logger.getLogger("SiderMessageChannel");

    private final SiderMessageManagerImpl manager;

    private final byte[] channel;

    private final Multimap<Class<? extends SiderMessage>, MessageRegistration<SiderMessage>> listeners =
            MultimapBuilder.hashKeys().linkedListValues().build();

    protected SiderMessageChannelImpl(SiderMessageManagerImpl manager, String channel) {
        this.manager = manager;
        this.channel = SafeEncoder.encode(channel);

        manager.getService().execute(() -> {
            try (Jedis jedis = manager.getSider().getPool().getResource()) {
                jedis.subscribe(this, SafeEncoder.encode(channel));
            }
        });
    }

    public <T extends SiderMessage> void send(SiderMessage message, Class<T> responseClass,
                                              Supplier<T> responseCreator, MessageListener<T> responseCallback,
                                              String... recipients) {
        listen(responseClass, responseCreator, ((senderSiderId, responseMessage) -> {
            if (responseMessage.getNonce().equals(message.getNonce())) {
                responseCallback.onMessage(senderSiderId, responseMessage);
            }
        }), true, true);
        send(message, recipients);
    }

    public void send(SiderMessage message, String... recipients) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            SiderOutputStream sos = new SiderOutputStream(gzos);

            try {
                sos.writeUTF(manager.getSider().getSiderId());
                sos.writeInt(recipients.length);
                for (String recipient : recipients) {
                    sos.writeInt(HashUtil.hash(recipient));
                }
                sos.writeInt(HashUtil.hash(message.getClass().getName()));
                message.serialize(sos);
            } catch (IOException e) {
                logger.severe("Failed to write networkable message " + message.getClass().getSimpleName()
                        + " to channel " + new String(channel));
                throw new RuntimeException(e);
            }

            gzos.finish();

            manager.getService().execute(() -> {
                try (Jedis jedis = manager.getSider().getPool().getResource()) {
                    jedis.publish(channel, baos.toByteArray());
                }
            });
        } catch (IOException e) {
            logger.severe("Failed to send networkable message " + message.getClass().getSimpleName() + "  to channel " + new String(channel));
            throw new RuntimeException(e);
        }
    }

    public <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator,
                                                MessageListener<T> listener, boolean ignoreSameServer, boolean runOnce) {
        listeners.put(messageClass, (MessageRegistration<SiderMessage>) new MessageRegistration<>(messageCreator, listener, ignoreSameServer, runOnce));
    }

    public <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator,
                                                MessageListener<T> listener, boolean ignoreSameServer) {
        listen(messageClass, messageCreator, listener, ignoreSameServer, false);
    }

    public <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator,
                                                MessageListener<T> listener) {
        listen(messageClass, messageCreator, listener, true);
    }

    @Override
    public <T extends SiderMessage> void unregister(Class<T> messageClass) {
        listeners.removeAll(messageClass);
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {
        try {
            SiderInputStream sis = new SiderInputStream(new GZIPInputStream(new ByteArrayInputStream(message)));
            String senderSider = sis.readUTF();
            int hashedSender = HashUtil.hash(senderSider);
            int siderId = HashUtil.hash(manager.getSider().getSiderId());

            int recipients = sis.readInt();
            if (recipients > 0) {
                boolean shouldReceive = false;
                for (int i = 0; i < recipients; i++) {
                    if (sis.readInt() == siderId) {
                        shouldReceive = true;
                        // Don't break here because we still need to read the rest of the recipients
                    }
                }
                if (!shouldReceive) {
                    return;
                }
            }

            int messageTypeNameHash = sis.readInt();

            Class<? extends SiderMessage> messageClass = null;
            for (Class<? extends SiderMessage> clazz : listeners.keySet()) {
                if (HashUtil.hash(clazz.getName()) == messageTypeNameHash) {
                    messageClass = clazz;
                    break;
                }
            }

            if (messageClass == null) {
                return;
            }

            for (MessageRegistration<SiderMessage> registration : new ArrayList<>(listeners.get(messageClass))) {
                if (registration.isIgnoreSameServer() && hashedSender == siderId) {
                    continue;
                }

                SiderMessage siderMessage = registration.getMessageCreator().get();
                siderMessage.deserialize(sis);

                MessageListener<SiderMessage> listener = registration.getListener();
                if (siderMessage.isAsync()) {
                    manager.getService().execute(() -> listener.onMessage(senderSider, siderMessage));
                } else {
                    listener.onMessage(senderSider, siderMessage);
                }

                if (registration.isRunOnce()) {
                    listeners.remove(messageClass, registration);
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to read networkable message from channel " + new String(channel));
            throw new RuntimeException(e);
        }
    }

    private static class MessageRegistration<T extends SiderMessage> {

        private final Supplier<T> messageCreator;
        private final MessageListener<T> listener;
        private final boolean ignoreSameServer;
        private final boolean runOnce;

        public MessageRegistration(Supplier<T> messageCreator, MessageListener<T> listener, boolean ignoreSameServer, boolean runOnce) {
            this.messageCreator = messageCreator;
            this.listener = listener;
            this.ignoreSameServer = ignoreSameServer;
            this.runOnce = runOnce;
        }

        public Supplier<T> getMessageCreator() {
            return messageCreator;
        }

        public MessageListener<T> getListener() {
            return listener;
        }

        public boolean isIgnoreSameServer() {
            return ignoreSameServer;
        }

        public boolean isRunOnce() {
            return runOnce;
        }
    }

}
