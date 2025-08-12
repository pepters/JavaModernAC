package com.modernac.checks.aim;

import com.modernac.TestPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AggressiveComponentCheckTest {
    @Test
    public void triggersViolationOnLargePitch() {
        TestPlugin plugin = new TestPlugin();
        PlayerData data = new PlayerData(UUID.randomUUID());
        AggressiveComponentCheck check = new AggressiveComponentCheck(plugin, data);
        check.handle(new RotationData(0, 10));
        assertEquals(1, data.getVl());
    }

    @Test
    public void noViolationOnSmallPitch() {
        TestPlugin plugin = new TestPlugin();
        PlayerData data = new PlayerData(UUID.randomUUID());
        AggressiveComponentCheck check = new AggressiveComponentCheck(plugin, data);
        check.handle(new RotationData(0, 1));
        assertEquals(0, data.getVl());
    }
}
