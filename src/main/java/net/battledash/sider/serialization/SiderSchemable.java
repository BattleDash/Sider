package net.battledash.sider.serialization;

/**
 * A base holder for a {@link SerializationScheme}
 *
 * @param <T> The type of object to serialize
 */
public abstract class SiderSchemable<T> {

    private SerializationScheme<T> scheme;

    public SiderSchemable(SerializationScheme<T> scheme) {
        this.scheme = scheme;
    }

    public SerializationScheme<T> getSerializer() {
        return scheme;
    }

    public void setSerializer(SerializationScheme<T> scheme) {
        this.scheme = scheme;
    }

}
