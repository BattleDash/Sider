package net.battledash.sider;

import net.battledash.sider.depot.SiderDepot;
import net.battledash.sider.messages.SiderMessageManager;
import net.battledash.sider.messages.impl.SiderMessageManagerImpl;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class Sider {

    private final JedisPool jedisPool;
    private final SiderMessageManager messageManager;

    /**
     * An identifier for this Sider instance, used to
     * send messages to specific servers.
     */
    private String siderId;

    public Sider(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.messageManager = new SiderMessageManagerImpl(this);
        this.siderId = UUID.randomUUID().toString();
    }

    public Sider(String host, int port) {
        this(new JedisPool(host, port));
    }

    public JedisPool getPool() {
        return jedisPool;
    }

    public SiderMessageManager getMessageManager() {
        return messageManager;
    }

    public String getSiderId() {
        return siderId;
    }

    public void setSiderId(String siderId) {
        this.siderId = siderId;
    }

    public <T> SiderDepot<T> createDepot(Class<T> type, String name) {
        return new SiderDepot<>(this, type, name);
    }

}