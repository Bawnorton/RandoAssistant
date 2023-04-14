package com.bawnorton.randoassistant.render;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.entity.Penguin;
import com.bawnorton.randoassistant.render.model.PenguinModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PenguinRenderer extends MobEntityRenderer<Penguin, PenguinModel> {

    public PenguinRenderer(EntityRendererFactory.Context context) {
        super(context, new PenguinModel(context.getPart(PenguinModel.LAYER_LOCATION)), 0.4F);
        this.addFeature(new PenguinHeldItemLayer(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(Penguin entity) {
        if (entity.isBaby()) {
            return new Identifier(RandoAssistant.MOD_ID, "textures/entity/baby_penguin.png");
        }
        return new Identifier(RandoAssistant.MOD_ID, "textures/entity/penguin.png");
    }
}
