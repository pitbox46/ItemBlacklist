package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin implements ICapabilityProvider {
    @Inject(at = @At(value = "HEAD"), method = "markDirty")
    public void onMarkDirty(CallbackInfo ci) {
        if(this instanceof IInventory) {
            for(int i = 0; i < ((IInventory) this).getSizeInventory(); i++) {
                if(ItemBlacklist.BANNED_ITEMS.contains(((IInventory) this).getStackInSlot(i).getItem())) {
                    ((IInventory) this).setInventorySlotContents(i, ItemStack.EMPTY);
                }
            }
        }
        else {
            this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(cap -> {
                if(cap instanceof IItemHandlerModifiable) {
                    for (int i = 0; i < cap.getSlots(); i++) {
                        if (ItemBlacklist.BANNED_ITEMS.contains(cap.getStackInSlot(i).getItem())) {
                            ((IItemHandlerModifiable) cap).setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }
            });
        }
    }
}
