# Unified Crops

No more duplicate crops and seeds!

## Overview

This mod reduces the clutter from multiple mods adding the same crop to the game
by setting one as a "default item".

For example, both Pam's Harvestcraft (PHC), Farmer's Delight (FD), and Mama's Herbs and Harvest (MHH)
add a version of rice. By default, each version will show up in your world, so 
you'll have 3 different versions of rice to choose to farm. Even worse is the 
Mama's version of rice doesn't have the correct tag, so can't be used for making food.

With this mod, the default rice is Pam's Harvestcraft. If you find loot, it will PHC
rice. If you cut down some crops, the seeds will be PHC rice seeds. Etc.
Furthermore, all the recipes are modified appropriately. If for some reason, 
you need a different version, the mod adds "morph" recipes for easy one-to-one
conversion. 

## Specifics

This mod is data-driven, so you can change the default items and add more crops
as you wish. The file is located at `config/crop_data.json`. A default version
has been created with the following mods:

- Pam's Harvestcraft
- Farmer's Delight
- Sushi Go
- Mama's Herbs and Harvest

The default file can be found [in the Github](https://github.com/pitbox46/UnifiedCrops/blob/master/src/main/resources/crop_data.json)
if you ever need it. If no file exists in `config/crop_data.json`,
the default file is copied over to that location.