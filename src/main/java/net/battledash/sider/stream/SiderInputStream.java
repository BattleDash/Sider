package net.battledash.sider.stream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class SiderInputStream extends DataInputStream {

    public SiderInputStream(InputStream in) {
        super(in);
    }

    public UUID readUUID() throws IOException {
        long mostSignificantBits = readLong();
        long leastSignificantBits = readLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

}
