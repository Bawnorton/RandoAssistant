package com.bawnorton.randoassistant.render.model;


import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.entity.Penguin;
import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PenguinModel extends AnimalModel<Penguin> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(new Identifier(RandoAssistant.MOD_ID, "penguin"), "main");

    private final ModelPart body;
    public final ModelPart head;
    private final ModelPart leftFlipper;
    private final ModelPart rightFlipper;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;
    private float slidingAnimationProgress;
    private float swimmingAnimationProgress;

    public PenguinModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent, true, 4.75F, 0.0F, 1.5f, 2.0f, 24.0f);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftFlipper = this.body.getChild("leftFlipper");
        this.rightFlipper = this.body.getChild("rightFlipper");
        this.leftFoot = this.body.getChild("leftFoot");
        this.rightFoot = this.body.getChild("rightFoot");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData meshdefinition = new ModelData();
        ModelPartData partdefinition = meshdefinition.getRoot();
        partdefinition.addChild("head", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0F, -5.01F, -3.0F, 6.0F, 5.0F, 6.0F, new Dilation(0.0F))
                .uv(23, 0).cuboid(-1.0F, -2.0F, -5.0F, 2.01F, 2.01F, 2.01F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 15.0F, 0.0F));
        ModelPartData body = partdefinition.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -2.0F, -3.0F, 8.0F, 9.0F, 7.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 17.0F, 0.0F));
        body.addChild("leftFlipper", ModelPartBuilder.create().uv(18, 21).cuboid(0.01F, -1.0F, -3.01F, 1.0F, 8.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, -1.0F, 0.0F));
        body.addChild("rightFlipper", ModelPartBuilder.create().uv(18, 21).mirrored().cuboid(-1.01F, -1.0F, -3.01F, 1.0F, 8.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-4.0F, -1.0F, 0.0F));
        body.addChild("leftFoot", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0F, 0.0F, -2.0F, 3.0F, 0.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, 7.0F, -3.0F));
        body.addChild("rightFoot", ModelPartBuilder.create().uv(16, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 3.0F, 0.0F, 2.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-2.0F, 7.0F, -3.0F));
        return TexturedModelData.of(meshdefinition, 64, 64);
    }

    @Override
    public void animateModel(Penguin entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.animateModel(entity, limbSwing, limbSwingAmount, partialTick);
        this.slidingAnimationProgress = entity.getSlidingAnimationProgress(partialTick);
        this.swimmingAnimationProgress = entity.getSwimmingAnimationProgress(partialTick);
    }

    @Override
    public void setAngles(Penguin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.traverse().forEach(ModelPart::resetTransform);
        this.head.resetTransform();

        float swingSlowdownFactor = 0.3F;

        if (swimmingAnimationProgress > 0) {
            this.body.pitch += ModelUtil.interpolateAngle(this.body.pitch, (float) Math.toRadians(90), this.swimmingAnimationProgress)
                    - MathHelper.cos(0.7F * ageInTicks) * (swingSlowdownFactor * 0.25F);
            this.body.pivotY = MathHelper.lerp(this.swimmingAnimationProgress, this.body.getDefaultTransform().pivotY, this.body.getDefaultTransform().pivotY + 7);
            this.body.pivotY += -MathHelper.cos(0.7F * ageInTicks) * (swingSlowdownFactor * 0.025F);

            this.head.pitch = MathHelper.lerp(this.swimmingAnimationProgress, headPitch * MathHelper.RADIANS_PER_DEGREE, 0);
            this.head.yaw = MathHelper.lerp(this.swimmingAnimationProgress, netHeadYaw * MathHelper.RADIANS_PER_DEGREE, 0);
            this.head.pivotY = MathHelper.lerp(this.swimmingAnimationProgress, this.head.getDefaultTransform().pivotY, entity.isBaby() ? 21 : 24);
            this.head.pivotZ = MathHelper.lerp(this.swimmingAnimationProgress, this.head.getDefaultTransform().pivotZ, -2);
            this.head.pitch += MathHelper.cos(0.7F * ((float) Math.toRadians(-40) + ageInTicks)) * (swingSlowdownFactor * 0.3F);

            this.leftFoot.pitch += (Math.toRadians(17.5) - MathHelper.cos((float) Math.toRadians(-40) + ageInTicks)) * swingSlowdownFactor;
            this.rightFoot.pitch += (Math.toRadians(17.5) - MathHelper.sin((float) Math.toRadians(-40) + ageInTicks)) * swingSlowdownFactor;

            this.leftFlipper.pitch += MathHelper.cos((float) Math.toRadians(-80) + ageInTicks) * (swingSlowdownFactor * 0.2F);
            this.leftFlipper.roll += (Math.toRadians(-5) - MathHelper.cos((float) Math.toRadians(-80) + ageInTicks)) *  (swingSlowdownFactor * 0.25F);
            this.rightFlipper.pitch += MathHelper.cos((float) Math.toRadians(-80) + ageInTicks) * (swingSlowdownFactor * 0.2F);
            this.rightFlipper.roll += (Math.toRadians(5) - MathHelper.cos((float) Math.toRadians(-80) + ageInTicks)) *  (swingSlowdownFactor * 0.25F);
        } else if (slidingAnimationProgress > 0) {
            this.body.pitch += ModelUtil.interpolateAngle(this.body.pitch, (float) Math.toRadians(90), this.slidingAnimationProgress);
            this.body.pivotY = MathHelper.lerp(this.slidingAnimationProgress, this.body.getDefaultTransform().pivotY, this.body.getDefaultTransform().pivotY + 7);
            this.body.pivotZ += (-MathHelper.cos(2F * limbSwing)) * swingSlowdownFactor * limbSwingAmount;

            this.head.pitch = MathHelper.lerp(this.slidingAnimationProgress, headPitch * MathHelper.RADIANS_PER_DEGREE, 0);
            this.head.yaw = MathHelper.lerp(this.slidingAnimationProgress, netHeadYaw * MathHelper.RADIANS_PER_DEGREE, 0);
            this.head.pivotY = MathHelper.lerp(this.slidingAnimationProgress, this.head.getDefaultTransform().pivotY, entity.isBaby() ? 20 : 24);
            this.head.pivotZ = MathHelper.lerp(this.slidingAnimationProgress, this.head.getDefaultTransform().pivotZ, -4);
            this.head.pivotY += -MathHelper.cos(2F * ((float)Math.toRadians(-80) + limbSwing)) * swingSlowdownFactor * limbSwingAmount;
            this.head.pivotZ += -MathHelper.cos(2F * limbSwing) * swingSlowdownFactor * limbSwingAmount;

            this.leftFoot.pitch += ModelUtil.interpolateAngle(this.leftFoot.pitch, (float) Math.toRadians(90), this.slidingAnimationProgress);
            this.rightFoot.pitch += ModelUtil.interpolateAngle(this.rightFoot.pitch, (float) Math.toRadians(90), this.slidingAnimationProgress);

            this.leftFlipper.roll += (Math.toRadians(-2.5) - MathHelper.cos(2F * limbSwing)) * (swingSlowdownFactor * 0.5F) * limbSwingAmount;
            this.rightFlipper.roll += (Math.toRadians(2.5) - MathHelper.cos(2F * limbSwing)) * (swingSlowdownFactor * 0.5F) * limbSwingAmount;
        } else {
            this.body.yaw += MathHelper.cos((float)Math.toRadians(-20) + limbSwing) * swingSlowdownFactor * limbSwingAmount;
            this.body.roll += MathHelper.cos(limbSwing) * (swingSlowdownFactor * 0.5F) * limbSwingAmount;

            this.head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE;
            this.head.yaw = netHeadYaw * MathHelper.RADIANS_PER_DEGREE;
            this.head.yaw += -MathHelper.cos((float)Math.toRadians(-80) + limbSwing) * (swingSlowdownFactor * 0.5F) * limbSwingAmount;
            this.head.roll += -MathHelper.cos((float)Math.toRadians(-40) + limbSwing) * (swingSlowdownFactor * 0.5F) * limbSwingAmount;
            this.head.pivotX += MathHelper.cos(limbSwing) * (swingSlowdownFactor * 0.1F) * limbSwingAmount;

            this.leftFoot.pitch += (Math.toRadians(-10) + MathHelper.cos(limbSwing)) * (swingSlowdownFactor * 2F) * limbSwingAmount;
            this.rightFoot.pitch += (Math.toRadians(-10) - MathHelper.cos(limbSwing)) * (swingSlowdownFactor * 2F) * limbSwingAmount;

            this.leftFlipper.roll += (Math.toRadians(-10) + MathHelper.cos((float)Math.toRadians(-40) + limbSwing)) * (swingSlowdownFactor * 0.8F) * limbSwingAmount;
            this.rightFlipper.roll += (Math.toRadians(10) + MathHelper.cos((float)Math.toRadians(-40) + limbSwing)) * (swingSlowdownFactor * 0.8F) * limbSwingAmount;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        getHeadParts().forEach(part -> part.render(matrices, vertices, light, overlay, red, green, blue, 0.4f));
        getBodyParts().forEach(part -> part.render(matrices, vertices, light, overlay, red, green, blue, 0.4f));
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.body);
    }
}
