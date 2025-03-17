package github.pitbox46.unifiedcrops.mixin;

import com.google.gson.JsonElement;
import github.pitbox46.unifiedcrops.UnifiedCrops;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow public abstract Collection<RecipeHolder<?>> getRecipes();

    @Shadow public abstract void replaceRecipes(Iterable<RecipeHolder<?>> recipes);

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;size()I")
    )
    //TODO: Get the logger message to display the correct value
    private void modifyRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
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
                        Item val = UnifiedCrops.cropMap().get(itemValue.item().getItem());
                        if (val == null) {
                            continue;
                        }
                        values[i] = new Ingredient.ItemValue(new ItemStack(val, itemValue.item().getCount()));
                    }
                }
            }
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        //Add morph recipes
        for(Map.Entry<Item, Item> entry : UnifiedCrops.cropMap().entrySet()) {
            recipes.add(new RecipeHolder<>(
                    createMapRL(entry.getKey(), entry.getValue()),
                    new ShapelessRecipe(
                            "",
                            CraftingBookCategory.MISC,
                            new ItemStack(entry.getKey()),
                            NonNullList.copyOf(List.of(Ingredient.of(entry.getValue())))
                    )
            ));
            recipes.add(new RecipeHolder<>(
                    createMapRL(entry.getValue(), entry.getKey()),
                    new ShapelessRecipe(
                            "",
                            CraftingBookCategory.MISC,
                            new ItemStack(entry.getValue()),
                            NonNullList.copyOf(List.of(Ingredient.of(entry.getKey(), entry.getKey())))
                    )
            ));
        }
        replaceRecipes(recipes);
    }

    private static ResourceLocation createMapRL(Item item1, Item item2) {
        return ResourceLocation.fromNamespaceAndPath(
                UnifiedCrops.MODID,
                item1.toString().replace(":", ".")
                        + "-"
                        + item2.toString().replace(":", ".")
        );
    }
}
