package net.battledash.sider.serialization;

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
