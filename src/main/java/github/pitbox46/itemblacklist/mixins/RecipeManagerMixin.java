package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(at = @At(value = "RETURN"), method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;", cancellable = true)
    private <I extends RecipeInput, T extends Recipe<I>> void onGetRecipe(RecipeType<T> pRecipeType, I pInput, Level pLevel, @Nullable RecipeHolder<T> holder, CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir) {
        if (Config.BAN_CRAFTING.getAsBoolean()) {
            cir.getReturnValue().ifPresent(value ->
                    cir.setReturnValue(ItemBlacklist.shouldDelete(
                            value.value().assemble(pInput, pLevel.registryAccess())) ?
                            Optional.empty():Optional.of(value)
                    )
            );
        }
    }
}
