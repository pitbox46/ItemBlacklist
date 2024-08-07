package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(at = @At(value = "RETURN"), method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", cancellable = true)
    private <I extends RecipeInput, T extends Recipe<I>> void onGetRecipe(RecipeType<T> pRecipeType, I pInput, Level pLevel, CallbackInfoReturnable<Optional<T>> cir) {
        cir.getReturnValue().ifPresent(value ->
                cir.setReturnValue(ItemBlacklist.shouldDelete(value.getResultItem(pLevel.registryAccess())) ? Optional.empty() : Optional.of(value)));
    }

    @Inject(at = @At(value = "RETURN"), method = "getRecipesFor", cancellable = true)
    private <I extends RecipeInput, T extends Recipe<I>> void onGetRecipes(RecipeType<T> pRecipeType, I pInput, Level pLevel, CallbackInfoReturnable<List<T>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(entry -> !ItemBlacklist.shouldDelete(entry.assemble(pInput, pLevel.registryAccess())))
                .collect(Collectors.toList()));
    }
}
