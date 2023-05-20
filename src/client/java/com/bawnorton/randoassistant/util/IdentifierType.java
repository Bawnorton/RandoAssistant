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

    private Identifier id;

    public static IdentifierType fromId(Identifier id) {
        if(id == null) throw new IllegalArgumentException("Identifier cannot be null");
        if(Registries.ENTITY_TYPE.containsId(id)) return ENTITY.withId(id);
        if(Registries.ITEM.containsId(id)) return ITEM.withId(id);
        if(Registries.BLOCK.containsId(id)) return BLOCK.withId(id);
        return OTHER.withId(id);
    }

    private IdentifierType withId(Identifier id) {
        this.id = id;
        return this;
    }

    public boolean isItemAndEntity() {
        return Registries.ENTITY_TYPE.containsId(id) && Registries.ITEM.containsId(id);
    }

    public static String getName(Identifier identifier, boolean preferEntity) {
        IdentifierType type = IdentifierType.fromId(identifier);
        if(type.isItemAndEntity()) {
            type = preferEntity ? IdentifierType.ENTITY : IdentifierType.ITEM;
        }
        return switch (type) {
            case ITEM -> Registries.ITEM.get(identifier).getName().getString();
            case BLOCK -> Registries.BLOCK.get(identifier).getName().getString();
            case ENTITY -> Registries.ENTITY_TYPE.get(identifier).getName().getString();
            case OTHER -> {
                String path = identifier.getPath();
                RecipeType recipeType = RecipeType.fromName(path);
                if (recipeType != null) {
                    String[] parts = path.split("/");
                    String name = parts[parts.length - 1].replaceAll("_", " ");
                    name = Arrays.stream(name.split(" ")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
                    yield name + " Recipe";
                } else if(path.contains("chest")) {
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
                    colour = Arrays.stream(colour.split(" ")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
                    yield colour.substring(0, 1).toUpperCase() + colour.substring(1).toLowerCase() + " Sheep";
                } else if (path.contains("fishing")) {
                    String[] parts = path.split("/");
                    String name = parts[parts.length - 1].replaceAll("_", " ");
                    if(name.equals("fishing")) yield "Fishing";
                    name = Arrays.stream(name.split(" ")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
                    yield "Fishing " + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                } else if (path.contains("piglin")) {
                    yield "Piglin Bartering";
                } else if (path.contains("cat")) {
                    yield "Cat Morning Gift";
                }
                yield path;
            }
        };
    }
}
