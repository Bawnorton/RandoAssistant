package com.bawnorton.randoassistant.tracking.trackable;

import com.google.common.collect.Sets;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

import java.util.Set;

public class LootedTrackable extends Trackable<Identifier> {
    public LootedTrackable(Stat<Identifier> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return getContent();
    }

    @Override
    public Set<String> getSearchableStrings() {
        Set<String> strings = Sets.newHashSet();
        strings.add(getIdentifier().toString());
        return strings;
    }
}
