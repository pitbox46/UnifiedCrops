package github.pitbox46.unifiedcrops;

import com.buuz135.sushigocrafting.proxy.SushiContent;
import com.mamailes.herbsandharvest.init.MHHItems;
import com.pam.pamhc2crops.setup.ItemRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.*;
import java.util.stream.Collectors;

public class CropMapDataProvider {
    public static final boolean GEN_CROP_MAP = false;

    protected static List<CropData> gather() {
        //Seeds
        Map<String, List<Holder<Item>>> seedMap = new HashMap<>();

        List<DeferredHolder<Item, ? extends Item>> pamsHarvestCraft = new ArrayList<>(ItemRegistration.ITEMS.getEntries());
        List<DeferredHolder<Item, ? extends Item>> farmersDelight = new ArrayList<>(ModItems.ITEMS.getEntries());
        List<DeferredHolder<Item, ? extends Item>> herbsAndHarvest = new ArrayList<>(MHHItems.ITEMS.getEntries());
        List<DeferredHolder<Item, ? extends Item>> sushiGo = new ArrayList<>(SushiContent.Items.REGISTRY.getEntries());

        addToMap(seedMap, pamsHarvestCraft, "seeditem");
        addToMap(seedMap, farmersDelight, "_seeds");
        addToMap(seedMap, herbsAndHarvest, "_seeds");
        addToMap(seedMap, sushiGo, "_seeds");

        List<CropData> seeds = seedMap.values().stream()
                .filter(l -> l.size() > 1)
                .map(l -> {
                    Holder<Item> defaultItem = l.removeFirst();
                    return new CropData(HolderSet.direct(l), defaultItem);
                }).collect(Collectors.toCollection(ArrayList::new));

        //Crops
        Map<String, List<Holder<Item>>> cropMap = new HashMap<>();

        addToMap(cropMap, pamsHarvestCraft, "item");
        addToMap(cropMap, farmersDelight, "");
        addToMap(cropMap, herbsAndHarvest, "");
        addToMap(cropMap, sushiGo, "");

        List<CropData> crops = cropMap.values().stream()
                .filter(l -> l.size() > 1)
                .map(l -> {
                    Holder<Item> defaultItem = l.removeFirst();
                    return new CropData(HolderSet.direct(l), defaultItem);
                }).collect(Collectors.toCollection(ArrayList::new));
        seeds.addAll(crops);

        //Manual mappings
        seeds.add(new CropData(
                HolderSet.direct(SushiContent.Items.SOY_SEEDS),
                ItemRegistration.soybeanseeditem
        ));
        seeds.add(new CropData(
                HolderSet.direct(SushiContent.Items.SOY_BEAN),
                ItemRegistration.soybeanitem
        ));

        return seeds;
    }

    protected static void addToMap(Map<String, List<Holder<Item>>> dataMap, Collection<DeferredHolder<Item, ? extends Item>> items, String qualifier) {
        items.removeIf(holder -> addToMap(dataMap, holder, qualifier));
    }

    protected static boolean addToMap(Map<String, List<Holder<Item>>> dataMap, DeferredHolder<Item, ? extends Item> holder, String qualifier) {
        ResourceLocation itemID = holder.getId();
        String itemName = itemID.getPath();
        if (itemName.endsWith(qualifier)) {
            dataMap.computeIfAbsent(
                    itemName.replace(qualifier, ""),
                    key -> new ArrayList<>()
            ).add(holder);
            return true;
        }
        return false;
    }
}
