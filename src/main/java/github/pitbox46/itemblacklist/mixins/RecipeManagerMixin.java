package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
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
    @Inject(at = @At(value = "RETURN"), method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", cancellable = true)
    private <C extends Container, T extends Recipe<C>> void onGetRecipe(RecipeType<T> pRecipeType, C pInventory, Level pLevel, CallbackInfoReturnable<Optional<T>> cir) {
        if (Config.BAN_CRAFTING.get()) {
            cir.getReturnValue().ifPresent(value ->
                    cir.setReturnValue(ItemBlacklist.shouldDelete(value.getResultItem(pLevel.registryAccess())) ? Optional.empty() : Optional.of(value)));
        }
    }

    @Inject(at = @At(value = "RETURN"), method = "getRecipesFor", cancellable = true)
    private <C extends Container, T extends Recipe<C>> void onGetRecipes(RecipeType<T> pRecipeType, C pInventory, Level pLevel, CallbackInfoReturnable<List<T>> cir) {
        if (Config.BAN_CRAFTING.get()) {
            cir.setReturnValue(cir.getReturnValue().stream()
                    .filter(entry -> !ItemBlacklist.shouldDelete(entry.assemble(pInventory, pLevel.registryAccess())))
                    .collect(Collectors.toList()));
        }
    }
}
