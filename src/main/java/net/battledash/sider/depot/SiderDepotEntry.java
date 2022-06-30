package net.battledash.sider.depot;
import java.util.Map;
import java.util.Objects;

public class SiderDepotEntry<V> implements Map.Entry<String, V> {

    private final SiderDepot<V> depot;
    private final String key;
    private final V value;

    public SiderDepotEntry(SiderDepot<V> depot, String key, V value) {
        this.depot = depot;
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        return depot.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SiderDepotEntry &&
                Objects.equals(key, ((SiderDepotEntry<?>) o).key) &&
                Objects.equals(value, ((SiderDepotEntry<?>) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

}