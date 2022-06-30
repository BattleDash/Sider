package net.battledash.sider.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class SiderOutputStream extends DataOutputStream {

    public SiderOutputStream(OutputStream out) {
        super(out);
    }

    public void writeUUID(UUID uuid) throws IOException {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

}
