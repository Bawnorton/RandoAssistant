package com.bawnorton.randoassistant.util;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum IdentifierType {
    ITEM,
    BLOCK,
    ENTITY,
    OTHER;

    public static IdentifierType fromId(Identifier id) {
        if(id == null) throw new IllegalArgumentException("Identifier cannot be null");
        if(Registries.ENTITY_TYPE.containsId(id)) return ENTITY;
        if(Registries.ITEM.containsId(id)) return ITEM;
        if(Registries.BLOCK.containsId(id)) return BLOCK;
        return OTHER;
    }

    public static boolean isItemAndEntity(Identifier id) {
        return Registries.ENTITY_TYPE.containsId(id) && Registries.ITEM.containsId(id);
    }

    public static String getName(Identifier identifier, boolean preferEntity) {
        IdentifierType type = IdentifierType.fromId(identifier);
        if(IdentifierType.isItemAndEntity(identifier)) {
            type = preferEntity ? IdentifierType.ENTITY : IdentifierType.ITEM;
        }
        return switch (type) {
            case ITEM -> Registries.ITEM.get(identifier).getName().getString();
            case BLOCK -> Registries.BLOCK.get(identifier).getName().getString();
            case ENTITY -> Registries.ENTITY_TYPE.get(identifier).getName().getString();
            case OTHER -> {
                String path = identifier.getPath();
                if(path.contains("chest")) {
                    String[] parts = path.split("/");
                    String name = parts[parts.length - 1].replaceAll("_", " ");
                    name = Arrays.stream(name.split(" ")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
                    yield name + (name.endsWith("Chest") ? "" :  " Chest");
                } else if (path.contains("hero")) {
                    String[] parts = path.split("/");
                    String name = parts[parts.length - 1].replaceAll("_", " ");
                    String[] nameParts = name.split(" ");
                    nameParts[0] = nameParts[0] + "'s";
                    name = Arrays.stream(nameParts).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
                    yield name;
                } else if (path.contains("sheep")) {
                    String[] parts = path.split("/");
                    String colour = parts[parts.length - 1].replaceAll("_", " ");
                    yield colour.substring(0, 1).toUpperCase() + colour.substring(1).toLowerCase() + " Sheep";
                } else if (path.contains("fishing")) {
                    String[] parts = path.split("/");
                    String name = parts[parts.length - 1].replaceAll("_", " ");
                    if(name.equals("fishing")) yield "Fishing";
                    yield "Fishing " + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                }
                yield path;
            }
        };
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
