package com.bawnorton.randoassistant.util;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public enum IdentifierType {
    ITEM,
    BLOCK,
    ENTITY,
    OTHER;

    public static IdentifierType fromId(Identifier id) {
        if(id == null) throw new IllegalArgumentException("Identifier cannot be null");
        if(Registries.ITEM.containsId(id)) return ITEM;
        if(Registries.BLOCK.containsId(id)) return BLOCK;
        if(Registries.ENTITY_TYPE.containsId(id)) return ENTITY;
        return OTHER;
    }

    public boolean isItem() {
        return this == ITEM;
    }

    public boolean isBlock() {
        return this == BLOCK;
    }

    public boolean isEntity() {
        return this == ENTITY;
    }

    public boolean isOther() {
        return this == OTHER;
    }
}
