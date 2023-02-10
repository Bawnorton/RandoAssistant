package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.screen.widget.ItemWidget;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import org.abego.treelayout.Configuration;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

public class LootTableScreen extends LightweightGuiDescription {
    public LootTableScreen() {
        double levelGap = 5;
        double nodeGap = 10;

        ItemWidget root = new ItemWidget();
        DefaultConfiguration<ItemWidget> configuration = new DefaultConfiguration<>(levelGap, nodeGap, Configuration.Location.Left, Configuration.AlignmentInLevel.AwayFromRoot);
        DefaultTreeForTreeLayout<ItemWidget> tree = new DefaultTreeForTreeLayout<>(root);


    }

    private void createTree(DefaultTreeForTreeLayout<ItemWidget> tree, ItemWidget parent) {

    }
}
