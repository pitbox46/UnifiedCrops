package github.pitbox46.unifiedcrops.mixin;

import github.pitbox46.unifiedcrops.UnifiedCrops;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    /**
     * Starts compiling {@link UnifiedCrops#CROP_MAP}
     * @param instance
     * @param fn
     * @return
     * @param <U>
     */
    @Redirect(
            method = "loadResources",
            at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;")
    )
    private static <U> CompletableFuture<U> addStartListener(CompletableFuture<LayeredRegistryAccess<RegistryLayer>> instance, Function<LayeredRegistryAccess<RegistryLayer>, ? extends CompletionStage<U>> fn) {
        return instance
                .whenComplete((future, e) -> UnifiedCrops.createFuture(future.compositeAccess()))
                .thenCompose(fn);
    }
}
