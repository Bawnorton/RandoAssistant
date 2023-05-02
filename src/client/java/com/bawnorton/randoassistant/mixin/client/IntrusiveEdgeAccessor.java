package com.bawnorton.randoassistant.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "org.jgrapht.graph.IntrusiveEdge", remap = false)
public interface IntrusiveEdgeAccessor {
    @Accessor
    Object getSource();

    @Accessor
    Object getTarget();

    @Accessor
    void setSource(Object source);

    @Accessor
    void setTarget(Object target);
}
