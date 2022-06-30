package net.battledash.sider.depot;

import net.battledash.sider.Sider;
import net.battledash.sider.serialization.SerializationScheme;
import net.battledash.sider.serialization.SiderSchemable;
import net.battledash.sider.serialization.types.GsonSerializationScheme;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SiderDepot<V> extends SiderSchemable<V> implements Map<String, V> {

    private final Sider sider;
    private final byte[] key;

    public SiderDepot(Sider sider, Class<V> type, String name) {
        super(new GsonSerializationScheme<>(type));
        this.sider = sider;
        this.key = SafeEncoder.encode(name);
    }

    @Override
    public int size() {
        try (Jedis jedis = sider.getPool().getResource()) {
            return (int) jedis.hlen(key);
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        try (Jedis jedis = sider.getPool().getResource()) {
            return jedis.hexists(this.key, SafeEncoder.encode(key.toString()));
        }
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }

        try (Jedis jedis = sider.getPool().getResource()) {
            SerializationScheme<V> serializer = getSerializer();

            for (byte[] val : jedis.hvals(key)) {
                if (val != null && value.equals(serializer.deserialize(val))) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public V get(Object key) {
        try (Jedis jedis = sider.getPool().getResource()) {
            byte[] val = jedis.hget(this.key, SafeEncoder.encode(key.toString()));

            if (val != null) {
                return getSerializer().deserialize(val);
            }

            return null;
        }
    }

    private V put(String key, V value, boolean absent) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try (Jedis jedis = sider.getPool().getResource()) {
            V previous = get(key);
            if (absent) {
                jedis.hsetnx(this.key, SafeEncoder.encode(key), value == null ? null : getSerializer().serialize(value));
            } else {
                jedis.hset(this.key, SafeEncoder.encode(key), value == null ? null : getSerializer().serialize(value));
            }
            return previous;
        }
    }

    @Override
    public V put(String key, V value) {
        return put(key, value, false);
    }

    @Override
    public V putIfAbsent(String key, V value) {
        return put(key, value, true);
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            return null;
        }

        try (Jedis jedis = sider.getPool().getResource()) {
            V previous = get(key);
            jedis.hdel(this.key, SafeEncoder.encode(key.toString()));
            return previous;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        if (m == null) {
            throw new IllegalArgumentException("Map cannot be null");
        }

        if (m.size() == 0) {
            return;
        }

        try (Jedis jedis = sider.getPool().getResource()) {
            Map<byte[], byte[]> map = new HashMap<>();

            for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
                map.put(SafeEncoder.encode(entry.getKey()), getSerializer().serialize(entry.getValue()));
            }

            jedis.hmset(key, map);
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = sider.getPool().getResource()) {
            jedis.del(key);
        }
    }

    @Override
    public Set<String> keySet() {
        try (Jedis jedis = sider.getPool().getResource()) {
            return jedis.hkeys(key).stream().map(String::new).collect(Collectors.toSet());
        }
    }

    @Override
    public Collection<V> values() {
        try (Jedis jedis = sider.getPool().getResource()) {
            List<V> list = new ArrayList<>();

            for (byte[] val : jedis.hvals(key)) {
                if (val != null) {
                    list.add(getSerializer().deserialize(val));
                }
            }

            return list;
        }
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        try (Jedis jedis = sider.getPool().getResource()) {
            Set<Entry<String, V>> entries = new HashSet<>();

            for (Map.Entry<byte[], byte[]> entry : jedis.hgetAll(key).entrySet()) {
                SiderDepotEntry<V> e = new SiderDepotEntry<>(this,
                        new String(entry.getKey()),
                        getSerializer().deserialize(entry.getValue())
                );
                entries.add(e);
            }

            return entries;
        }
    }

}
