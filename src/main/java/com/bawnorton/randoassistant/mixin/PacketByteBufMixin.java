package com.bawnorton.randoassistant.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {
    @Shadow public abstract PacketByteBuf writeString(String string);
    @Shadow public abstract PacketByteBuf writeVarInt(int value);

    @Shadow public abstract String readString();

    @Inject(method = "writeRegistryValue", at = @At("HEAD"), cancellable = true)
    private <T> void onWriteRegistryValue(IndexedIterable<T> registry, T value, CallbackInfo ci) {
        int i = registry.getRawId(value);
        if(registry.equals(Registries.CUSTOM_STAT) && i == -1) {
            i = (int) Math.pow(2, 25);
            Identifier id = (Identifier) value;
            writeVarInt(i);
            writeString(id.getNamespace());
            writeString(id.getPath());
            ci.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "readRegistryValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;get(I)Ljava/lang/Object;"))
    private <T> T onReadRegistryValue(IndexedIterable<T> registry, int i) {
        if(registry.equals(Registries.CUSTOM_STAT) && i == (int) Math.pow(2, 25)) {
            String namespace = readString();
            String path = readString();
            Identifier id = new Identifier(namespace, path);
            return (T) id;
        }
        return registry.get(i);
    }
}
