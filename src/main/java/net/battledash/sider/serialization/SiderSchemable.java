package net.battledash.sider.serialization;

public abstract class SiderSchemable<T> {

    private SerializationScheme<T> scheme;

    public SiderSchemable(SerializationScheme<T> scheme) {
        this.scheme = scheme;
    }

    public void setSerializer(SerializationScheme<T> scheme) {
        this.scheme = scheme;
    }

    public SerializationScheme<T> getSerializer() {
        return scheme;
    }

}
