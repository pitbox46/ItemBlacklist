package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {
    @Shadow @Final public List<Slot> inventorySlots;

    @Shadow public abstract NonNullList<ItemStack> getInventory();

    @Inject(at = @At(value = "HEAD"), method = "detectAndSendChanges")
    public void onDetectAndSendChanges(CallbackInfo ci) {
        for(int i = 0; i < this.inventorySlots.size(); ++i) {
            if(ItemBlacklist.shouldDelete(this.getInventory().get(i))) {
                this.getInventory().set(i, ItemStack.EMPTY);
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "onContainerClosed")
    public void onContainerClosed(PlayerEntity playerIn, CallbackInfo ci) {
        for(int i = 0; i < this.inventorySlots.size(); ++i) {
            if(ItemBlacklist.shouldDelete(this.getInventory().get(i))) {
                this.getInventory().set(i, ItemStack.EMPTY);
            }
        }
        for(int i = 0; i < playerIn.inventory.getSizeInventory(); ++i) {
            if(ItemBlacklist.shouldDelete(playerIn.inventory.getStackInSlot(i))) {
                playerIn.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }
    }
}
