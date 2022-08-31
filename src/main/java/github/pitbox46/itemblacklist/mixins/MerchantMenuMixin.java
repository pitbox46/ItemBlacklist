package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {
    @Inject(at = @At(value = "RETURN"), method = "getOffers")
    private void getOffers(CallbackInfoReturnable<MerchantOffers> cir) {
        if(cir.getReturnValue() != null)
            cir.getReturnValue().removeIf(offer -> ItemBlacklist.shouldDelete(offer.assemble()));
    }
}
