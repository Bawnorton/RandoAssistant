package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Random;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Shadow protected EntityModel<LivingEntity> model;

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void onRender(Args args) {
        try {
            if(!Config.getInstance().enableAprilFools) return;
            Random random = new Random(MinecraftClient.getInstance().hashCode() + model.hashCode());
            int rgb = random.nextInt(16777216);
            float red = (rgb >> 16) & 0xFF;
            float green = (rgb >> 8) & 0xFF;
            float blue = rgb & 0xFF;
            args.set(4, red);
            args.set(5, green);
            args.set(6, blue);
            args.set(2, 15728880);
        } catch (Exception ignored) {}
    }
}
