package io.github.retrooper.packetevents.wrapper.play.client;

import io.github.retrooper.packetevents.event.PacketPlayReceiveEvent;

public class WrapperPlayClientUseEntity {
    public enum Action { ATTACK }
    public WrapperPlayClientUseEntity(PacketPlayReceiveEvent event) {}
    public Action getAction() { return Action.ATTACK; }
}
