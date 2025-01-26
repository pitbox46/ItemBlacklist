package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(RecipeMap.class)
public class RecipeMapMixin {
    @Inject(at = @At(value = "RETURN"), method = "getRecipesFor", cancellable = true)
    private <I extends RecipeInput, T extends Recipe<I>> void onGetRecipes(RecipeType<T> pRecipeType, I pInput, Level pLevel, CallbackInfoReturnable<Stream<RecipeHolder<T>>> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .filter(entry -> !ItemBlacklist.shouldDelete(entry.value().assemble(pInput, pLevel.registryAccess())))
        );
    }
}
