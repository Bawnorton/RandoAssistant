package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "dropLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onDropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci, Identifier identifier, LootTable lootTable, LootContext.Builder builder) {
        if(causedByPlayer && source.getSource() instanceof ServerPlayerEntity serverPlayer) {
            List<ItemStack> drops = lootTable.generateLoot(builder.build(LootContextTypes.ENTITY));
            SerializeableLootTable serializeableLootTable = SerializeableLootTable.ofEntity(((LivingEntity) (Object) this).getType(), drops);
            Networking.sendLootTablePacket(serverPlayer, serializeableLootTable);
        }
    }
}
