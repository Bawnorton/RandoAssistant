package com.bawnorton.randoassistant.render.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RenderManager {
    private static final  MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<BlockPos, Renderer> renderers = new HashMap<>();

    public static void addRenderer(BlockPos pos, Renderer renderer) {
        renderers.put(pos, renderer);
    }

    public static void renderRenderers(MatrixStack matrixStack, Camera camera) {
        matrixStack.push();

        Vec3d camPos = camera.getPos();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.lineWidth(20.0f);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos, Renderer> entry : renderers.entrySet()) {
            BlockPos pos = entry.getKey();
            if(isValidBlock(pos)) {
                entry.getValue().render(matrixStack, buffer, camPos);
            } else {
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(renderers::remove);

        if (buffer.isBuilding()) {
            tessellator.draw();
        }
        RenderSystem.disableBlend();

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    private static boolean isValidBlock(BlockPos pos) {
        Block block = null;
        if (client.world != null) {
            BlockState state = client.world.getBlockState(pos);
            if(state.getFluidState().isEmpty() && !state.isAir()) {
                block = state.getBlock();
            }
        }
        return block != null;
    }

    public static void clearRenderers() {
        renderers.clear();
    }
}
