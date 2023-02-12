package com.bawnorton.randoassistant.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.geom.Rectangle2D;

public class ResetPositionWidget extends DrawableHelper {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    Rectangle2D.Float bounds;
    final GraphDisplay graphDisplay;

    public ResetPositionWidget(GraphDisplay graphDisplay) {
        super();
        this.graphDisplay = graphDisplay;
    }

    public Tooltip render(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        int SIZE = 26;
        bounds = new Rectangle2D.Float(x - SIZE / 2f - 5, y - SIZE / 2f - 5, SIZE, SIZE);
        Tooltip tooltip = null;
        if (bounds.contains(mouseX, mouseY)) {
            RenderSystem.setShaderColor(0.75F, 0.75F, 0.75F, 1F);
            tooltip = Tooltip.of(Text.of("Reset Position"));
        } else {
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }
        matrices.push();
        matrices.translate(0, 0, 100);
        drawTexture(matrices, x - SIZE / 2 - 5, y - SIZE / 2 - 5, 26, 128, SIZE, SIZE);
        ItemStack icon = new ItemStack(Items.COMPASS);
        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
        matrices.pop();
        return tooltip;
    }

    public InputResult handleMouseDown(int x, int y) {
        if (bounds != null && bounds.contains(x, y)) {
            graphDisplay.resetOffset();
            return InputResult.PROCESSED;
        }
        return InputResult.IGNORED;
    }
}
