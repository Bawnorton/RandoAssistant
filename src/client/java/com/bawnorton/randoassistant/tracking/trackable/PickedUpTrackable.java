package com.bawnorton.randoassistant.tracking.trackable;

import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

import java.util.Set;

public class PickedUpTrackable extends Trackable<Item> {
    public PickedUpTrackable(Stat<Item> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ITEM.getId(getContent());
    }

    @Override
    public Set<String> getSearchableStrings() {
        Set<String> strings = Sets.newHashSet();
        strings.add(getIdentifier().toString());
        strings.add(getContent().getName().getString());
        return strings;
    }
}
