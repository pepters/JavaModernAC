package com.modernac.checks.aim;

import com.modernac.TestPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleHeuristicCheckTest {
    @Test
    public void triggersViolationOnLargeYaw() {
        TestPlugin plugin = new TestPlugin();
        PlayerData data = new PlayerData(UUID.randomUUID());
        SimpleHeuristicCheck check = new SimpleHeuristicCheck(plugin, data);
        check.handle(new RotationData(15, 0));
        assertEquals(1, data.getVl());
    }

    @Test
    public void noViolationOnSmallYaw() {
        TestPlugin plugin = new TestPlugin();
        PlayerData data = new PlayerData(UUID.randomUUID());
        SimpleHeuristicCheck check = new SimpleHeuristicCheck(plugin, data);
        check.handle(new RotationData(1, 0));
        assertEquals(0, data.getVl());
    }
}
