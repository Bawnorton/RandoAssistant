package com.bawnorton.randoassistant.event.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.networking.client.Networking;
import com.bawnorton.randoassistant.render.overlay.Colour;
import com.bawnorton.randoassistant.render.overlay.Cube;
import com.bawnorton.randoassistant.render.overlay.Cuboid;
import com.bawnorton.randoassistant.render.overlay.RenderManager;
import com.bawnorton.randoassistant.tracking.Tracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.glfw.GLFW;

public class EventManager {

    public static KeyBinding highlight = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.randoassistant.highlight",
            GLFW.GLFW_KEY_V,
            "key.categories.randoassistant"
    ));

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            RandoAssistantClient.isInstalledOnServer = false;
            Config.getInstance().enableOverride = false;
            Tracker.getInstance().clear();
            Networking.requestHandshakePacket();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RenderManager.clearRenderers();
            if(highlight.isPressed()) {
                ClientPlayerEntity player = client.player;
                if(player == null) return;
                BlockPos pos = player.getBlockPos();
                int radius = Config.getInstance().highlightRadius;
                for(int x = -radius; x < radius; x++) {
                    for(int y = -radius; y < radius; y++) {
                        for(int z = -radius; z < radius; z++) {
                            BlockPos blockPos = pos.add(x, y, z);
                            if(client.world == null) return;

                            BlockState state = client.world.getBlockState(blockPos);
                            Block block = state.getBlock();
                            Identifier id = Registries.BLOCK.getId(block);
                            if(Tracker.getInstance().isEnabled(id)) continue;

                            Cube cube = new Cube(blockPos, Colour.fromHex(0xFF0000));
                            RenderManager.addRenderer(blockPos, cube);
                        }
                    }
                }
            }
        });
    }
}
