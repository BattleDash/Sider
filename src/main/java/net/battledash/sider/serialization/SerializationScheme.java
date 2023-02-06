package net.battledash.sider.serialization;

import net.battledash.sider.depot.SiderDepot;
import net.battledash.sider.messages.SiderMessage;
import net.battledash.sider.serialization.types.GsonSerializationScheme;
import net.battledash.sider.stream.SiderInputStream;
import net.battledash.sider.stream.SiderOutputStream;

/**
 * A serialization scheme is a way to serialize and deserialize generic
 * objects to and from a binary representation.
 * <p>
 * A default {@link GsonSerializationScheme} is included with Sider,
 * but you can implement your own if you wish. This is an alternative
 * to overriding the {@link SiderMessage#serialize(SiderOutputStream)}
 * and {@link SiderMessage#deserialize(SiderInputStream)} methods, and
 * a way to implement custom encoding of {@link SiderDepot} entries
 *
 * @param <T> The type of object to serialize
 */
public abstract class SerializationScheme<T> {

    protected Class<T> type;

    public SerializationScheme(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public abstract byte[] serialize(T t);

    public abstract T deserialize(byte[] b);

}
