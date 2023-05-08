package com.bawnorton.randoassistant.tracking.trackable;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

import java.util.Set;

public class MinedTrackable extends Trackable<Block> {
    public MinedTrackable(Stat<Block> minedStat) {
        super(minedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.BLOCK.getId(getContent());
    }

    @Override
    public Set<String> getSearchableStrings() {
        Set<String> strings = Sets.newHashSet();
        strings.add(getIdentifier().toString());
        strings.add(getContent().getName().getString());
        return strings;
    }
}
