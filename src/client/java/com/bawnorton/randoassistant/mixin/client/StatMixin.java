package com.bawnorton.randoassistant.mixin.client;

import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Stat.class)
public abstract class StatMixin {
    @Redirect(method = "getName(Lnet/minecraft/stat/StatType;Ljava/lang/Object;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;", ordinal = 1))
    private static <T> Identifier getName(Registry<T> registry, T t) {
        return t instanceof Identifier ? (Identifier) t : registry.getId(t);
    }
}
