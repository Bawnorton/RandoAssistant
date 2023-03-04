# Random Drop Assistant

This is a mod that provides a hierarchical drop tracking graph for Minecraft with random drops.

[![Time Spent](https://wakatime.com/badge/user/d3cfc009-c727-4c07-bf46-94032e69d457/project/87bd5b80-7bb8-45de-a574-cc6f38f8fff3.svg)](https://wakatime.com/badge/user/d3cfc009-c727-4c07-bf46-94032e69d457/project/87bd5b80-7bb8-45de-a574-cc6f38f8fff3)

### <span style="color:#CC1010">PLEASE READ</span>
#### This mod is quite techincal and still in development, please read the entire description before using it.

## GUI

Open the GUI with the red book in the inventory. 
- #### Graph
  - The gui will display a graph of nodes, containg all the discovered loot tables in the game as well as interactions in the world.
    - Interactions are things like smelting, brewing, stripping logs, etc.
  - Each node contains a block, item or entity.
- #### Paths
  - Paths connect nodes to each other and when a node is selected, it's paths will render.
  - Paths are coloured, directional and will point from the parent node to the child node.
    - Red paths are paths to **blocks / entities** that drop the selected node. (Parents)
    - Blue paths are paths to **blocks / items** that are dropped by the selected node. (Children)
    - Yellow paths are paths to **blocks / items** that are gotten from **crafting / interacting** with the selected item / block in the world.
      - Interactions can be parent or child nodes.
- #### Search Bar (At the top of the gui) 
  - Type in the name of a block or entity to search for it and the graph will center on it.
  - Search is either "exact", "contains" or "fuzzy". Search ignores whitespaces and is case-insensitive.
    - Exact: The search term must match the name of the node exactly.
    - Contains: The search term must be contained in the name of the node.
    - Fuzzy: The search term must closey match the name of the node.
      - This uses levenshtein distance to determine closeness, thus, length of the search term plays a big part in the result.
- #### Buttons and Sliders
  - There is a repositioning button in the bottom left which will reposition and scale the graph back to the origin.
  - In the bottom right there are 2 buttons and a slider.
    - The slider will change which line is displayed, left-most slider position is all lines.
      - As you move the slider to left, the length of the paths will decrease, such that, Line 1 is the shortest path to get the selected item.
      - This is useful for when the graph is cluttered and you want to clearly see the paths to a specific node.
    - The "Hide Other Nodes" toggle button will re-render the graph with only the nodes associated with the selected node.
      - When in selected node mode, the button will change to "Show Other Nodes" and will re-render the graph with all nodes.
      - Depending on the size of the graph, or the number of selected nodes, this can take a second or two.
    - The "Hide Children" toggle button will hide the connections to children nodes, thus, reducing screen clutter.
- #### Zooming
  - Use the scroll wheel to zoom in and out.
  - This uses minecraft's gui scale setting and will reset back when the ui is closed.
    - This is the cleanest way to implement zooming as minecraft does not play nice with scaling gui elements any other way, unfortunately this means that all the buttons will also scale.

### Keybinds
Press `k` to reveal all drops. This will fill the graph with all blocks and entities that have a drop.<br>
Press `j` to remove all drops from the graph. 


## Dependencies
- [Minecraft Fabric 1.19.3](https://fabricmc.net/)
- [Fabric API 0.73.2+1.19.3](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

## Installation
- Download the latest release from releases.
- Download the latest release of [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api).
- Place the jar files in the mods folder
