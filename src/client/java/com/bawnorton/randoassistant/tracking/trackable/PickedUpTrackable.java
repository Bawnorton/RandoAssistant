package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.item.Item;
import net.minecraft.stat.Stat;

public class PickedUpTrackable extends Trackable<Item> {
    protected PickedUpTrackable(Stat<Item> associatedStat) {
        super(associatedStat);
    }
}
