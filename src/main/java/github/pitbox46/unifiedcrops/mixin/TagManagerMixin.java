package github.pitbox46.unifiedcrops.mixin;

import github.pitbox46.unifiedcrops.CropData;
import github.pitbox46.unifiedcrops.UnifiedCrops;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(TagManager.class)
public class TagManagerMixin {
    /**
     * Adds our custom tags at runtime
     */
    @Inject(
            method = "lambda$createLoader$3",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;loadAndBuild(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"),
            cancellable = true
    )
    private static void onLoad(ResourceKey<? extends Registry<Item>> resourcekey, TagLoader<Holder<Item>> tagloader, ResourceManager resourceManager, CallbackInfoReturnable<TagManager.LoadResult<Item>> cir) {
        if (resourcekey.equals(Registries.ITEM)) {
            Map<ResourceLocation, Collection<Holder<Item>>> map = tagloader.loadAndBuild(resourceManager);

            int totalTagsAdded = 0;
            for (CropData cropData : UnifiedCrops.CROP_DATA) {
                //Adds all tags from the children item to the default item
                //TODO Look into performance
                for (Map.Entry<ResourceLocation, Collection<Holder<Item>>> entry : map.entrySet()) {
                    ResourceLocation rl = entry.getKey();
                    Collection<Holder<Item>> itemsInTag = entry.getValue();

                    for (Holder<Item> holder : cropData.items()) {
                        if (!itemsInTag.contains(holder)) {
                            continue;
                        }

                        List<Holder<Item>> list = new ArrayList<>(itemsInTag);
                        list.add(cropData.defaultItem());
                        map.put(rl, list);
                        totalTagsAdded++;
                    }
                }

                //Adds our custom tags
                List<Holder<Item>> list = new ArrayList<>();
                cropData.items().unwrap().ifRight(list::addAll);
                list.add(cropData.defaultItem());
                map.put(cropData.getTag(), list);
                totalTagsAdded++;
            }
            UnifiedCrops.LOGGER.info("{} tags added", totalTagsAdded);

            TagManager.LoadResult<Item> loadResults = new TagManager.LoadResult<>(resourcekey, map);
            cir.setReturnValue(loadResults);
        }
    }
}
