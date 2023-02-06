package net.battledash.sider.messages;

import net.battledash.sider.Sider;

import java.util.function.Supplier;

/**
 * A channel used to send messages across Redis pub/sub, obtained from {@link SiderMessageManager}
 */
public interface SiderMessageChannel {

    /**
     * Send a message across the channel with an optional list of recipients
     *
     * @param message The message to send
     * @param recipients The optional recipients of the message, denoted on the receiving end with
     * {@link Sider#setSiderId(String)}
     */
    void send(SiderMessage message, String... recipients);

    /**
     * Send a message and expect a response
     *
     * @param message The message to send
     * @param responseClass The class of the response message
     * @param responseCreator A creator for the response message
     * @param responseCallback A callback to handle the response
     * @param recipients The optional recipients of the message
     * @param <T> The class of the response message
     */
    <T extends SiderMessage> void send(SiderMessage message, Class<T> responseClass,
                                       Supplier<T> responseCreator, MessageListener<T> responseCallback,
                                       String... recipients);

    /**
     * Listen for messages on the channel
     * Differs from {@link SiderMessageChannel#listen(Class, Supplier, MessageListener, boolean)}
     *
     * @param messageClass The class of message to listen for
     * @param messageCreator The creator for the message class
     * @param listener The method to receive messages on
     * @param <T> The class of message to listen for
     */
    <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator, MessageListener<T> listener);

    /**
     * Listen for messages on the channel
     *
     * @param messageClass The class of message to listen for
     * @param messageCreator The creator for the message class
     * @param listener The method to receive messages on
     * @param ignoreSameServer If false, messages sent from this sider instance will be received.
     * @param <T> The class of message to listen for
     */
    <T extends SiderMessage> void listen(Class<T> messageClass, Supplier<T> messageCreator, MessageListener<T> listener, boolean ignoreSameServer);

    /**
     * Unregister all listeners for a specific message class
     *
     * @param messageClass The class of message to unregister
     * @param <T> The class of message to unregister
     */
    <T extends SiderMessage> void unregister(Class<T> messageClass);

    @FunctionalInterface
    interface MessageListener<T extends SiderMessage> {

        void onMessage(String senderSiderId, T message);

    }

}
