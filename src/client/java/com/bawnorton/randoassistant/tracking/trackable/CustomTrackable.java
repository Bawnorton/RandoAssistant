package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.util.Identifier;

public class CustomTrackable {
    private final Identifier identifier;
    private boolean enabled = false;

    public CustomTrackable(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    @Override
    public String toString() {
        return "CustomTrackable{" +
                "identifier=" + identifier +
                ", enabled=" + enabled +
                '}';
    }
}
