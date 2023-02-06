package net.battledash.sider.messages;

import net.battledash.sider.Sider;

/**
 * A message manager controls access to {@link SiderMessageChannel}
 * for an instance of {@link Sider}
 */
public interface SiderMessageManager {

    /**
     * Retrieve and cache a {@link SiderMessageChannel} for a channel name
     *
     * @param channel The channel name
     * @return The channel
     */
    SiderMessageChannel getChannel(String channel);

}
