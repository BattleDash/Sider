package net.battledash.sider.messages;

import java.util.function.Supplier;

public interface SiderMessageChannel {

    void send(SiderMessage message, String... recipients);

    <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator, MessageListener<T> listener, boolean ignoreSameServer);

    <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator, MessageListener<T> listener);

    <T extends SiderMessage> void unregister(Class<T> messageClass);

    @FunctionalInterface
    interface MessageListener<T extends SiderMessage> {

        void onMessage(String senderSiderId, T message);

    }

}
