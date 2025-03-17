package github.pitbox46.unifiedcrops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public record CropData(HolderSet<Item> items, Holder<Item> defaultItem) implements Predicate<ItemStack> {
    public static final Codec<CropData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(CropData::items),
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("default_item").forGetter(CropData::defaultItem)
            ).apply(instance, CropData::new)
    );
    public static final Codec<List<CropData>> LIST_CODEC = CODEC.listOf();

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(items);
    }

    public boolean test(Item item) {
        return items.contains(item.builtInRegistryHolder());
    }

    public ResourceLocation getTag() {
        return ResourceLocation.fromNamespaceAndPath(
                UnifiedCrops.MODID,
                defaultItem().getRegisteredName().replace(":", ".")
        );
    }

    public static ResourceLocation createMappedRL(Item item1, Item item2) {
        return ResourceLocation.fromNamespaceAndPath(
                UnifiedCrops.MODID,
                item1.toString().replace(":", ".")
                        + "-"
                        + item2.toString().replace(":", ".")
        );
    }

    public static ItemStack convert(ItemStack stack) {
        Item convertedItem = UnifiedCrops.getDefaultCrop(stack.getItem());
        if (convertedItem == null) {
            return stack;
        }
        return new ItemStack(convertedItem, stack.getCount());
    }

    public static JsonElement encodeToJson(HolderLookup.Provider registryAccess, List<CropData> cropData) {
        return LIST_CODEC.encodeStart(registryAccess.createSerializationContext(JsonOps.INSTANCE), cropData)
                .result()
                .orElseThrow();
    }

    public static List<CropData> decodeFromJson(HolderLookup.Provider registryAccess, JsonArray json) {
        return LIST_CODEC.parse(registryAccess.createSerializationContext(JsonOps.INSTANCE), json)
                .resultOrPartial(m -> UnifiedCrops.LOGGER.warn("Could not read blacklist: {}", m))
                .orElseGet(List::of);
    }
}
