package com.bawnorton.randoassistant.compat.yacl;

import com.bawnorton.randoassistant.config.Config;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.OptionGroup;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YACLImpl {
    public static Screen getScreen() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Random Assistant Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .tooltip(Text.literal("General settings"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Main"))
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Display Star Icons"))
                                        .tooltip(Text.literal("Display star icons on unbroken blocks"))
                                        .binding(true, () -> Config.getInstance().unbrokenBlockIcon, (value) -> Config.getInstance().unbrokenBlockIcon = value)
                                        .controller(TickBoxController::new)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Enable Toasts"))
                                        .tooltip(Text.literal("Enable toasts that display the load/save status of the mod (Failure reporting toasts are always enabled)"))
                                        .binding(true, () -> Config.getInstance().toasts, (value) -> Config.getInstance().toasts = value)
                                        .controller(TickBoxController::new)
                                        .build())
                                .option(Option.createBuilder(Config.SearchType.class)
                                        .name(Text.literal("Search Type"))
                                        .tooltip(Text.literal("The type of search to use when searching for blocks"))
                                        .binding(Config.SearchType.CONTAINS, () -> Config.getInstance().searchType, (value) -> Config.getInstance().searchType = value)
                                        .controller(EnumController::new)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Debug"))
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Debug Mode"))
                                        .tooltip(Text.literal("Enable debug mode"))
                                        .binding(false, () -> Config.getInstance().debug, (value) -> Config.getInstance().debug = value)
                                        .controller(TickBoxController::new)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.literal("Child Depth"))
                                        .tooltip(Text.literal("The maximum depth to search for child nodes"))
                                        .binding(100, () -> Config.getInstance().childDepth, (value) -> Config.getInstance().childDepth = value)
                                        .controller(IntegerFieldController::new)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.literal("Parent Depth"))
                                        .tooltip(Text.literal("The maximum depth to search for parent nodes"))
                                        .binding(100, () -> Config.getInstance().parentDepth, (value) -> Config.getInstance().parentDepth = value)
                                        .controller(IntegerFieldController::new)
                                        .build())
                                .collapsed(true)
                                .build())
                        .build())
                .build()
                .generateScreen(null);
    }
}
