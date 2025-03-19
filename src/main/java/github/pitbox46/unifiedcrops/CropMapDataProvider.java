package github.pitbox46.unifiedcrops;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.stream.Collectors;

public class CropMapDataProvider {
    protected static List<CropData> gather() {
        //Seeds
        Map<String, List<Holder<Item>>> seedMap = new HashMap<>();

        BuiltInRegistries.ITEM.keySet().forEach(rl -> {
            if (!Config.CROP_MODS.get().contains(rl.getNamespace())) {
                return;
            }

            String cleanPath = rl.getPath()
                    .replaceAll("_|item", "")
                    .toLowerCase();

            String key = cleanPath;
            if (cleanPath.matches(".*(seed|seeds).*")) {
                key = cleanPath.replaceFirst("seed|seeds", "seed");
            }

            seedMap.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(BuiltInRegistries.ITEM.getHolder(rl).orElseThrow());
        });

        List<CropData> crops = seedMap.values().stream()
                .filter(l -> l.size() > 1) //Filter out entries of size 1
                .peek(l -> l.sort((x, y) -> { //Sort list so that pamhc2crops takes precedent
                    var rl1 = x.getKey().location();
                    var rl2 = y.getKey().location();
                    if (rl1.equals(rl2)) {
                        return 0;
                    } else if (rl1.getNamespace().equals(Config.CROP_MODS.get().getFirst())) {
                        return 1;
                    } else {
                        return rl1.compareTo(rl2);
                    }
                }))
                .map(l -> { //Set the first item to be the default item
                    Holder<Item> defaultItem = l.removeLast();
                    return new CropData(HolderSet.direct(l), defaultItem);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return crops;
    }
}
