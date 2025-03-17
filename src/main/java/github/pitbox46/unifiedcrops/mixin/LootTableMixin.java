package github.pitbox46.unifiedcrops.mixin;

import github.pitbox46.unifiedcrops.UnifiedCrops;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Shadow public abstract ResourceLocation getLootTableId();

    /**
     * Replaces generated loot with their converted form
     */
    @Redirect(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;modifyLoot(Lnet/minecraft/resources/ResourceLocation;Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;")
    )
    private ObjectArrayList<ItemStack> consumeItemStack(ResourceLocation mod, ObjectArrayList<ItemStack> objectarraylist, LootContext context) {
        ObjectArrayList<ItemStack> loot = CommonHooks.modifyLoot(getLootTableId(), objectarraylist, context);
        for (int i = 0, len = loot.size(); i < len; i++) {
            ItemStack stack = loot.get(i);
            ItemStack convertedStack = UnifiedCrops.convertStack(stack);
            if (stack == convertedStack) {
                continue;
            }
            loot.set(i, convertedStack);
        }
        return loot;
    }
}
