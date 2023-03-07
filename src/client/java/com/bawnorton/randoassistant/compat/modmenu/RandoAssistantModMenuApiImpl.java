package com.bawnorton.randoassistant.compat.modmenu;

import com.bawnorton.randoassistant.config.ConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class RandoAssistantModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigManager.getConfigScreen();
    }
}
