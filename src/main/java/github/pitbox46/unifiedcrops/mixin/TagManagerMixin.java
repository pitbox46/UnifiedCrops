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
    private static <T> void onLoad(ResourceKey<? extends Registry<T>> resourcekey, TagLoader<Holder<T>> tagloader, ResourceManager resourceManager, CallbackInfoReturnable<TagManager.LoadResult<T>> cir) {
        if (resourcekey.equals(Registries.ITEM)) {
            Map<ResourceLocation, Collection<Holder<T>>> map = tagloader.loadAndBuild(resourceManager);

            for (CropData cropData : UnifiedCrops.CROP_DATA) {
                List<Holder<?>> list = new ArrayList<>();
                cropData.items().unwrap().ifRight(list::addAll);
                list.add(cropData.defaultItem());
                map.put(cropData.getTag(), (List<Holder<T>>) (Object) list);
            }

            TagManager.LoadResult<T> loadResults = new TagManager.LoadResult<>(resourcekey, map);
            cir.setReturnValue(loadResults);
        }
    }
}
