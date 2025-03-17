package github.pitbox46.unifiedcrops.mixin;

import com.google.common.collect.Multimap;
import github.pitbox46.unifiedcrops.CropData;
import github.pitbox46.unifiedcrops.UnifiedCrops;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract Collection<RecipeHolder<?>> getRecipes();
    @Shadow public abstract void replaceRecipes(Iterable<RecipeHolder<?>> recipes);
    @Shadow private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow @Final private HolderLookup.Provider registries;

    /**
     * Changes recipe ingredients and adds morph recipes
     */
    @Redirect(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V")
    )
    private void modifyRecipes(Logger instance, String s, Object o) {
        List<RecipeHolder<?>> recipes = getRecipes().stream().peek(recipeHolder -> {
            Recipe<?> recipe = recipeHolder.value();

            //Loop through each value of each ingredient and replace them if needed
            //TODO Replace with a tag
            for (var ingredient : recipe.getIngredients()) {
                if (ingredient.isCustom()) {
                    continue;
                }
                Ingredient.Value[] values = ingredient.getValues();
                for (int i = 0; i < values.length; i++) {
                    if (values[i] instanceof Ingredient.ItemValue itemValue) {
                        CropData cropData = UnifiedCrops.cropMap().get(itemValue.item().getItem());
                        if (cropData == null) {
                            continue;
                        }
                        values[i] = new Ingredient.TagValue(TagKey.create(Registries.ITEM, cropData.getTag()));
                    }
                }
            }
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        //Add morph recipes
        for(Map.Entry<Item, CropData> entry : UnifiedCrops.cropMap().entrySet()) {
            Item value = entry.getValue().defaultItem().value();
            recipes.add(new RecipeHolder<>(
                    CropData.createMappedRL(entry.getKey(), value),
                    new ShapelessRecipe(
                            "",
                            CraftingBookCategory.MISC,
                            new ItemStack(entry.getKey()),
                            NonNullList.copyOf(List.of(Ingredient.of(value)))
                    )
            ));
            recipes.add(new RecipeHolder<>(
                    CropData.createMappedRL(value, entry.getKey()),
                    new ShapelessRecipe(
                            "",
                            CraftingBookCategory.MISC,
                            new ItemStack(value),
                            NonNullList.copyOf(List.of(Ingredient.of(entry.getKey(), entry.getKey())))
                    )
            ));
        }
        replaceRecipes(recipes);
        LOGGER.info("Loaded {} recipes", this.byType.size());
    }
}
