package net.battledash.sider.tests;

import net.battledash.sider.Sider;
import net.battledash.sider.messages.SiderMessage;
import net.battledash.sider.messages.SiderMessageChannel;
import net.battledash.sider.messages.SiderMessageManager;
import net.battledash.sider.stream.SiderInputStream;
import net.battledash.sider.stream.SiderOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiderMessageTest {

    private SiderMessageManager manager;

    @BeforeEach
    public void setup() {
        Sider sider = new Sider(System.getenv("REDIS_HOST"), Integer.parseInt(System.getenv("REDIS_PORT")));
        manager = sider.getMessageManager();
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        SiderMessageChannel channel = manager.getChannel("siderChannelTest");

        String username = "Test";
        channel.send(new TestMessage(username));

        channel.listen(TestMessage.class, TestMessage::new, (s, m) -> {
            assertEquals(m.username, username);
            System.out.println("Received username: " + m.username + " from: " + s);
        }, false);

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

}
