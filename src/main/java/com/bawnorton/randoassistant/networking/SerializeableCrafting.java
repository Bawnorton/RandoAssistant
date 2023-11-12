package com.bawnorton.randoassistant.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SerializeableCrafting implements Serializeable {
    private final RecipeEntry<?> input;
    private final Item output;

    private SerializeableCrafting(RecipeEntry<?> input, Item output) {
        this.input = input;
        this.output = output;
    }

    public static SerializeableCrafting deserialize(PacketByteBuf buf) {
        Identifier serializer = buf.readIdentifier();
        Identifier id = buf.readIdentifier();
        Item output = buf.readItemStack().getItem();
        RecipeEntry<?> recipe = new RecipeEntry<Recipe<?>>(id, Registries.RECIPE_SERIALIZER.getOrEmpty(serializer).orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + serializer)).read(buf));
        return new SerializeableCrafting(recipe, output);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PacketByteBuf serialize() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(Registries.RECIPE_SERIALIZER.getId(input.value().getSerializer()));
        buf.writeIdentifier(input.id());
        buf.writeItemStack(output.getDefaultStack());
        RecipeSerializer serializer = input.value().getSerializer();
        serializer.write(buf, input.value());
        return buf;
    }

    public static SerializeableCrafting of(RecipeEntry<?> input, Item output) {
        return new SerializeableCrafting(input, output);
    }

    public RecipeEntry<?> getInput() {
        return input;
    }

    public Item getOutput() {
        return output;
    }

    @Override
    public Identifier getTypePacket() {
        return NetworkingConstants.CRAFTING_PACKET;
    }
}
