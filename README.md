# Random Drop Assistant

This is a mod that provides a hierachial drop tracking graph for Minecraft with random drops.

## GUI
Open the GUI with the red book in the inventory. 
- Each node contains a block or an entity.
- Paths connect nodes to each other and when a node is selected, it's paths will render.
  - Red paths are paths to blocks / entities that drop the selected item.
  - Blue paths are paths to blocks / entities that are dropped by the selected item.
- There is a search bar at the top of the GUI. 
  - Type in the name of a block or entity to search for it and the graph will center on it.
    - This uses levenshtein distance to find the closest match.
- There is a repositioning button in the bottom left which will reposition the graph back to the origin.
- GUI scales with minecraft's GUI scale setting.
  - Attempted to scale the GUI with the scroll wheel but it caused too many issues with rendering.

### Reveal All Drops
Press `k` to reveal all drops. This will fill the graph with all blocks and entities that have a drop.
- Warning: This will take a while to render and can cause a client side lag.

## Dependencies
- [Minecraft Fabric 1.19.3](https://fabricmc.net/)
- [Fabric API 0.73.2+1.19.3](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

## Installation
- Download the latest release from releases.
- Download the latest release of [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api).
- Place the jar files in the mods folder
