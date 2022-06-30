# <img align="center" src="https://i.battleda.sh/7a727a2213355927dab19f8bec935885.png" alt="Redis Logo" width="70"> sider

Sider is a Java library for useful [Redis](https://redis.io/) features, it uses [Jedis](https://github.com/redis/jedis/) internally.

Sider was created because I got sick of writing Redis boilerplate whenever I started new projects.
Its intended use case and design is for Minecraft Server Networks, but it can be used for any Redis-based system.

# Install

Sider is available on my repository.

Maven:
```xml
<repositories>
  <repository>
    <id>battledash</id>
    <url>https://nexus.battleda.sh/repository/maven-releases</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>net.battledash.sider</groupId>
    <artifactId>Sider</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

Gradle:
```groovy
repositories {
  maven { url 'https://nexus.battleda.sh/repository/maven-releases' }
}
dependencies {
  implementation 'me.battledash.sider:Sider:1.0.0'
}
```

# Usage

To begin, you need to create a Sider instance. You can pass in an already created JedisPool instance,
or simply a Redis login.

```java
Sider sider = new Sider(new JedisPool("localhost", 6379));
// OR
Sider sider = new Sider("localhost", 6379);
```

### Sider Depots

A depot is an implementation of a map, but stored using redis. It's essentially a wrapper for Redis hash tables.
```java
SiderDepot<Integer> depot = sider.createDepot(Integer.class, "sider_test");

depot.put("test1", 1);

assertEquals(1, depot.get("test1"));
```

Calling `SiderDepot#get` on a different server with a Depot of the same ID will return the same result.

Because Depots are an implementation of a map, even things like `SiderDepot#entrySet` work, using `SiderDepotEntry`. `Entry#setValue` will change the value stored in the Depot.

### Sider Networkable Messages

Sider's packet system is called Networkable Messages. By extending `SiderMessage`, you can easily create packets to send to different servers running Sider.

```java
class ServerTicketRequestMessage extends SiderMessage {
    private UUID playerId;
    private TicketRequestReason reason;
    
    public ServerTicketRequestMessage() {
        super(true); // Should listeners be called async?
    }
    
    // Setters, constructors, etc. However you want to set up message fields.
}
```

#### Sending messages

Messages use channels, so to send a message you first need to determine which
channel it belongs on.

```java
SiderMessageManager messageManager = sider.getMessageManager();
SiderMessageChannel messageChannel = messageManager.getChannel("join_ticketing");
```

Once you have the channel, you can send and receive messages on it.

Sending a message to all servers listening on the channel:
```java
messageChannel.send(new ServerTicketRequestMessage());
```

Sending a message to a specific sider instance:
```java
messageChannel.send(new ServerTicketRequestMessage(), "lobby1");
```

On the receiving end:
```java
sider.setSiderId("lobby1");

messageChannel.listen(
        ServerTicketRequestMessage.class,
        ServerTicketRequestMessage::new,
        (sender, message) -> ...
);
```

You can also include a final boolean parameter as false, if you want the server that sent to message to also receive it.

If you do not set a Sider ID yourself, a random UUID is used.
