package net.battledash.sider.serialization.types;

import com.google.gson.Gson;
import net.battledash.sider.serialization.SerializationScheme;

public class GsonSerializationScheme<T> extends SerializationScheme<T> {

    /**
     * Stored per-instance so that we can mess with Gson settings per-serializer.
     */
    private Gson gson = new Gson();

    public GsonSerializationScheme(Class<T> type) {
        super(type);
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public byte[] serialize(T o) {
        if (o == null) {
            return null;
        }

        return gson.toJson(o).getBytes();
    }

    @Override
    public T deserialize(byte[] b) {
        if (b == null) {
            return null;
        }

        return gson.fromJson(new String(b), type);
    }

}
