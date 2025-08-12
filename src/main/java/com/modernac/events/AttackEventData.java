package com.modernac.events;

import java.util.UUID;

public record AttackEventData(UUID attackerId, UUID victimId) {}
