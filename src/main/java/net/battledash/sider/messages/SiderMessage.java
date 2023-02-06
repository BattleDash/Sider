package net.battledash.sider.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import net.battledash.sider.stream.SiderInputStream;
import net.battledash.sider.stream.SiderOutputStream;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * The base class of all Sider networkable messages
 */
public abstract class SiderMessage {

    private static final Gson GSON = new Gson();

    private final UUID nonce;
    private final transient boolean async;

    public SiderMessage(UUID nonce, boolean async) {
        this.nonce = nonce;
        this.async = async;
    }

    public SiderMessage(UUID nonce) {
        this(nonce, true);
    }

    public SiderMessage() {
        this(UUID.randomUUID(), true);
    }

    public boolean isAsync() {
        return async;
    }

    public UUID getNonce() {
        return nonce;
    }

    public void serialize(SiderOutputStream stream) throws IOException {
        stream.writeUTF(GSON.toJson(this));
    }

    public void deserialize(SiderInputStream stream) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(getClass(), new SiderMessageDeserializer<>(this)).create();
        gson.fromJson(stream.readUTF(), getClass());
    }

    private static class SiderMessageDeserializer<T extends SiderMessage> implements InstanceCreator<T> {

        private final T message;

        public SiderMessageDeserializer(T message) {
            this.message = message;
        }

        @Override
        public T createInstance(Type type) {
            return message;
        }

    }

}
