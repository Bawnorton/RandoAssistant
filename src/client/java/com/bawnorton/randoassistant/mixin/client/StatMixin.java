package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(Stat.class)
public abstract class StatMixin {
    @WrapOperation(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;", ordinal = 1))
    private static <T> Identifier getName(Registry<T> registry, T object, Operation<Identifier> original) {
        return object instanceof Identifier ? (Identifier) object : original.call(registry, object);
    }
}
