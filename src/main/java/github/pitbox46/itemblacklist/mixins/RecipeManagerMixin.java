package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
//    @Inject(at = @At(value = "RETURN"), method = "getRecipes(Lnet/minecraft/item/crafting/IRecipeType;)Ljava/util/Map;", cancellable = true)
//    private <C extends IInventory, T extends IRecipe<C>> void onGetRecipes(IRecipeType<T> recipeTypeIn, CallbackInfoReturnable<Map<ResourceLocation, IRecipe<C>>> cir) {
//        cir.setReturnValue(cir.getReturnValue().entrySet().stream()
//                .filter(entry -> !ItemBlacklist.shouldDelete(entry.getValue().getRecipeOutput()))
//                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll));
//    }

    @Inject(at = @At(value = "RETURN"), method = "getRecipe(Lnet/minecraft/item/crafting/IRecipeType;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/world/World;)Ljava/util/Optional;", cancellable = true)
    private <C extends IInventory, T extends IRecipe<C>> void onGetRecipe(IRecipeType<T> recipeTypeIn, C inventoryIn, World worldIn, CallbackInfoReturnable<Optional<T>> cir) {
        cir.getReturnValue().ifPresent(value -> {
                Item items = cir.getReturnValue().get().getRecipeOutput().getItem();
                cir.setReturnValue(ItemBlacklist.shouldDelete(items.getDefaultInstance()) ? Optional.empty() : Optional.of(value));
        });
    }

    @Inject(at = @At(value = "RETURN"), method = "getRecipes(Lnet/minecraft/item/crafting/IRecipeType;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/world/World;)Ljava/util/List;", cancellable = true)
    private <C extends IInventory, T extends IRecipe<C>> void onGetRecipes(IRecipeType<T> recipeTypeIn, C inventoryIn, World worldIn, CallbackInfoReturnable<List<T>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(entry -> !ItemBlacklist.shouldDelete(entry.getCraftingResult(inventoryIn)))
                .collect(Collectors.toList()));
    }
}
