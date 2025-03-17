package github.pitbox46.unifiedcrops.mixin;

import github.pitbox46.unifiedcrops.UnifiedCrops;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Redirect(
            method = "loadResources",
            at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;")
    )
    private static <U> CompletableFuture<U> addStartListener(CompletableFuture<LayeredRegistryAccess<RegistryLayer>> instance, Function<LayeredRegistryAccess<RegistryLayer>, ? extends CompletionStage<U>> fn) {
        return instance
                .whenComplete((future, e) -> UnifiedCrops.createFuture(future.compositeAccess()))
                .thenCompose(fn);
    }

    @Inject(method = "loadResources", at = @At("RETURN"), cancellable = true)
    private static void addFinishListener(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> registries, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, Executor backgroundExecutor, Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        cir.setReturnValue(cir.getReturnValue().whenComplete(
                (rsr, throwable) -> UnifiedCrops.onServerResourcesLoaded(rsr)
        ));
    }
}
