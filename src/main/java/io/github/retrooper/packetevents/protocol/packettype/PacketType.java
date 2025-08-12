package io.github.retrooper.packetevents.protocol.packettype;

public class PacketType {
    public static class Play {
        public static class Client {
            public static final PacketType LOOK = new PacketType();
            public static final PacketType POSITION_LOOK = new PacketType();
            public static final PacketType USE_ENTITY = new PacketType();
        }
    }
}
