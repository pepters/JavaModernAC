package org.bukkit.entity;

import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;

public interface Player extends CommandSender {
    UUID getUniqueId();
    String getName();
    int getPing();
    AttributeInstance getAttribute(Attribute attribute);
    boolean isOnline();
}
