package net.battledash.sider.messages.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.battledash.sider.Sider;
import net.battledash.sider.messages.SiderMessage;
import net.battledash.sider.messages.SiderMessageChannel;
import net.battledash.sider.messages.SiderMessageManager;
import net.battledash.sider.serialization.SiderSchemable;
import net.battledash.sider.serialization.types.GsonSerializationScheme;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SiderMessageManagerImpl extends SiderSchemable<SiderMessage> implements SiderMessageManager {

    private final ExecutorService service = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("sider-message-pump-%d").build()
    );

    private final Map<String, SiderMessageChannel> channels = new HashMap<>();
    private final Sider sider;

    public SiderMessageManagerImpl(Sider sider) {
        super(new GsonSerializationScheme<>(SiderMessage.class));
        this.sider = sider;
    }

    public SiderMessageChannel getChannel(String channel) {
        return channels.computeIfAbsent(channel, s -> new SiderMessageChannelImpl(this, s));
    }

    public Sider getSider() {
        return sider;
    }

    protected ExecutorService getService() {
        return service;
    }

}
