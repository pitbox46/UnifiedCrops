package github.pitbox46.unifiedcrops;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

import java.io.File;
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
    public static final boolean GEN_CROP_MAP = false;

    public static List<CropData> CROP_DATA;
    public static FutureTask<Map<Item,CropData>> CROP_MAP_FUTURE;
    public static Lazy<Map<Item,CropData>> CROP_MAP = Lazy.of(() -> {
        try {
            return CROP_MAP_FUTURE.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    });

    public UnifiedCrops(ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemDropped(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            ItemStack converted = CropData.convert(stack);
            if (stack != converted) {
                itemEntity.setItem(converted);
            }
        }
    }

    public static ItemStack convertStack(ItemStack stack) {
        CropData data = CROP_MAP.get().get(stack.getItem());
        if (data == null) {
            return stack;
        }
        return new ItemStack(data.defaultItem().value(), stack.getCount());
    }

    public static Map<Item, CropData> cropMap() {
        return CROP_MAP.get();
    }

    public static Item getDefaultCrop(Item item) {
        CropData data = CROP_MAP.get().get(item);
        if (data == null) {
            return null;
        }
        return data.defaultItem().value();
    }

    /**
     * Creates the {@link UnifiedCrops#CROP_MAP_FUTURE} and submits the future to the background executor.
     * @param registryAccess
     */
    public static void createFuture(HolderLookup.Provider registryAccess) {
        File cropFile = JsonUtils.initialize(
                FMLPaths.CONFIGDIR.get(),
                "crop_data.json",
                registryAccess
        );
        CROP_DATA = JsonUtils.readFromJson(cropFile, registryAccess);

        CROP_MAP_FUTURE = new FutureTask<>(() -> {
            ImmutableMap.Builder<Item, CropData> mapBuilder = new ImmutableMap.Builder<>();
            BuiltInRegistries.ITEM.stream().forEach(item -> CROP_DATA.stream()
                    .filter(cropData -> cropData.test(item))
                    .forEach(cropData -> mapBuilder.put(item, cropData))
            );
            LOGGER.info("Crop map finished building");
            return mapBuilder.build();
        });
        BACKGROUND_THREAD.submit(CROP_MAP_FUTURE);
        LOGGER.info("Crop map future created");
    }

    /*TODO
        - Add compat with quests
     */
}
