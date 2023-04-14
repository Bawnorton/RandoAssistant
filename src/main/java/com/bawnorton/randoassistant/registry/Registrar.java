package com.bawnorton.randoassistant.registry;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.entity.Penguin;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Registrar {
    public static final EntityType<Penguin> PENGUIN = registerEntityType("penguin", Penguin::new, SpawnGroup.CREATURE, 0.7F, 0.9F, 10);
    public static final SoundEvent PENGUIN_AMBIENT = registerSoundEvent("penguin_ambient", SoundEvent.of(new Identifier(RandoAssistant.MOD_ID, "entity.penguin.ambient")));
    public static final SoundEvent PENGUIN_DEATH = registerSoundEvent("penguin_death", SoundEvent.of(new Identifier(RandoAssistant.MOD_ID, "entity.penguin.death")));
    public static final SoundEvent PENGUIN_HURT = registerSoundEvent("penguin_hurt", SoundEvent.of(new Identifier(RandoAssistant.MOD_ID, "entity.penguin.hurt")));

    public static void init() {
        RandoAssistant.LOGGER.debug("Registering entity types");
    }

    public static <T extends Entity> EntityType<T> registerEntityType(String name, EntityType.EntityFactory<T> factory, SpawnGroup category, float width, float height, int clientTrackingRange) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(RandoAssistant.MOD_ID, name), FabricEntityTypeBuilder.create(category, factory).dimensions(EntityDimensions.changing(width, height)).trackRangeChunks(clientTrackingRange).build());
    }

    public static <T extends SoundEvent> T registerSoundEvent(String name, T soundEvent) {
        return Registry.register(Registries.SOUND_EVENT, new Identifier(RandoAssistant.MOD_ID, name), soundEvent);
    }
}
