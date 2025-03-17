package github.pitbox46.unifiedcrops;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Mod(UnifiedCrops.MODID)
public class UnifiedCrops {
    public static final String MODID = "unifiedcrops";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ExecutorService BACKGROUND_THREAD = Executors.newSingleThreadExecutor();


    public static FutureTask<Map<Item,Item>> CROP_MAP_FUTURE;
    public static Lazy<Map<Item,Item>> CROP_MAP = Lazy.of(() -> {
        try {
            return CROP_MAP_FUTURE.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    });

    public UnifiedCrops(ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
    }

    public static ItemStack convertStack(ItemStack stack) {
        Item item = CROP_MAP.get().get(stack.getItem());
        if (item == null) {
            return stack;
        }
        return new ItemStack(item, stack.getCount());
    }

    @SubscribeEvent
    public void onItemDropped(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            ItemStack converted = CropData.convert(CROP_MAP.get(), stack);
            if (stack != converted) {
                itemEntity.setItem(converted);
            }
        }
    }

    public static Map<Item, Item> cropMap() {
        return CROP_MAP.get();
    }

    /**
     * Creates the {@link UnifiedCrops#CROP_MAP_FUTURE} and submits the future to the background executor.
     * @param registryAccess
     */
    public static void createFuture(HolderLookup.Provider registryAccess) {
        CROP_MAP_FUTURE = new FutureTask<>(() -> {
            File cropFile = JsonUtils.initialize(
                    FMLPaths.CONFIGDIR.get(),
                    "crop_data.json",
                    registryAccess
            );
            List<CropData> cropDataList = JsonUtils.readFromJson(cropFile, registryAccess);

            ImmutableMap.Builder<Item, Item> mapBuilder = new ImmutableMap.Builder<>();
            BuiltInRegistries.ITEM.stream().forEach(item -> cropDataList.stream()
                    .filter(cropData -> cropData.test(item))
                    .forEach(cropData -> mapBuilder.put(item, cropData.defaultItem().value()))
            );
            LOGGER.info("Crop map finished building");
            return mapBuilder.build();
        });
        BACKGROUND_THREAD.submit(CROP_MAP_FUTURE);
        LOGGER.info("Crop map future created");
    }

    /**
     * Fires on separate thread
     * @param rsr
     */
    public static void onServerResourcesLoaded(ReloadableServerResources rsr) {
    }

    /*TODO
        - Make sure they all have the same tags
        - Add compat with quests
     */
}
