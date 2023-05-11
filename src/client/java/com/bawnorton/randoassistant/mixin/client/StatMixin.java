package com.bawnorton.randoassistant.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Stat.class)
public abstract class StatMixin {
    @WrapOperation(method = "getName(Lnet/minecraft/stat/StatType;Ljava/lang/Object;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;", ordinal = 1))
    private static <T> Identifier getName(Registry<T> registry, T object, Operation<Identifier> original) {
        return object instanceof Identifier ? (Identifier) object : original.call(registry, object);
    }
}
