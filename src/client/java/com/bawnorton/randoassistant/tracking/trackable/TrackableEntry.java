package com.bawnorton.randoassistant.tracking.trackable;

import com.bawnorton.randoassistant.util.LootCondition;
import net.minecraft.util.Identifier;

public record TrackableEntry(Identifier identifier, LootCondition condition) {
    public boolean requiresSilkTouch() {
        return condition == LootCondition.SILK_TOUCH;
    }
}
