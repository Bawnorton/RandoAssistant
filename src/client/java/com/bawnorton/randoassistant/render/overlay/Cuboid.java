package com.bawnorton.randoassistant.render.overlay;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Cuboid extends Renderer {
    private final Line[] edges = new Line[12];
    public final BlockPos start;
    public final Vec3i size;

    public Cuboid(BlockPos start, Vec3i size, Colour color) {
        this.start = start;
        this.size = size;
        this.edges[0] = new Line(toVec3d(this.start), toVec3d(this.start.add(this.size.getX(), 0, 0)), color);
        this.edges[1] = new Line(toVec3d(this.start), toVec3d(this.start.add(0, this.size.getY(), 0)), color);
        this.edges[2] = new Line(toVec3d(this.start), toVec3d(this.start.add(0, 0, this.size.getZ())), color);
        this.edges[3] = new Line(toVec3d(this.start.add(this.size.getX(), 0, this.size.getZ())), toVec3d(this.start.add(this.size.getX(), 0, 0)), color);
        this.edges[4] = new Line(toVec3d(this.start.add(this.size.getX(), 0, this.size.getZ())), toVec3d(this.start.add(this.size.getX(), this.size.getY(), this.size.getZ())), color);
        this.edges[5] = new Line(toVec3d(this.start.add(this.size.getX(), 0, this.size.getZ())), toVec3d(this.start.add(0, 0, this.size.getZ())), color);
        this.edges[6] = new Line(toVec3d(this.start.add(this.size.getX(), this.size.getY(), 0)), toVec3d(this.start.add(this.size.getX(), 0, 0)), color);
        this.edges[7] = new Line(toVec3d(this.start.add(this.size.getX(), this.size.getY(), 0)), toVec3d(this.start.add(0, this.size.getY(), 0)), color);
        this.edges[8] = new Line(toVec3d(this.start.add(this.size.getX(), this.size.getY(), 0)), toVec3d(this.start.add(this.size.getX(), this.size.getY(), this.size.getZ())), color);
        this.edges[9] = new Line(toVec3d(this.start.add(0, this.size.getY(), this.size.getZ())), toVec3d(this.start.add(0, 0, this.size.getZ())), color);
        this.edges[10] = new Line(toVec3d(this.start.add(0, this.size.getY(), this.size.getZ())), toVec3d(this.start.add(0, this.size.getY(), 0)), color);
        this.edges[11] = new Line(toVec3d(this.start.add(0, this.size.getY(), this.size.getZ())), toVec3d(this.start.add(this.size.getX(), this.size.getY(), this.size.getZ())), color);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos) {
        if (this.start == null || this.size == null) return;

        for (Line edge : this.edges) {
            if (edge == null) continue;
            edge.render(matrixStack, vertexConsumer, cameraPos);
        }
    }
}