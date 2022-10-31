package net.battledash.sider.tests;

import net.battledash.sider.Sider;
import net.battledash.sider.messages.SiderMessage;
import net.battledash.sider.messages.SiderMessageChannel;
import net.battledash.sider.messages.SiderMessageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiderMessageTest {

    private SiderMessageManager manager;

    @BeforeEach
    public void setup() {
        Sider sider = new Sider("localhost", 6379);
        manager = sider.getMessageManager();
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        SiderMessageChannel channel = manager.getChannel("siderChannelTest");

        String username = "Test";
        channel.send(new TestMessage(username));

        channel.listen(TestMessage.class, TestMessage::new, (s, m) ->
                assertEquals(m.username, username), false);

        lock.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testMessageReply() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        SiderMessageChannel channel = manager.getChannel("siderChannelTest");

        String username = "Test";

        // This might fail because ignoreSameServer is implicitly true, will fix later
        channel.send(new TestMessage(username), TestMessageReply.class, TestMessageReply::new,
                ((senderSiderId, message) -> assertEquals(username, message.getUsername())));

        lock.await(5, TimeUnit.SECONDS);
    }

    static class TestMessage extends SiderMessage {

        private String username;

        TestMessage(String username) {
            this.username = username;
        }

        TestMessage() {
        }

    }

    static class TestMessageReply extends SiderMessage {

        private String username;

        TestMessageReply(String username) {
            this.username = username;
        }

        TestMessageReply() {
        }

        public String getUsername() {
            return username;
        }
    }

}
