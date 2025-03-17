package github.pitbox46.unifiedcrops.mixin;

import github.pitbox46.unifiedcrops.UnifiedCrops;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(LootPool.class)
public class LootPoolMixin {
    /**
     * Replaces generated loot with their converted form
     */
    @Redirect(method = "addRandomItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/functions/LootItemFunction;decorate(Ljava/util/function/BiFunction;Ljava/util/function/Consumer;Lnet/minecraft/world/level/storage/loot/LootContext;)Ljava/util/function/Consumer;"))
    private Consumer<ItemStack> consumeItemStack(BiFunction<ItemStack, LootContext, ItemStack> stackModification, Consumer<ItemStack> originalConsumer, LootContext lootContext) {
        Consumer<ItemStack> vanillaConsumer = LootItemFunction.decorate(stackModification, originalConsumer, lootContext);
        return itemStack -> vanillaConsumer.accept(UnifiedCrops.convertStack(itemStack));
    }
}
