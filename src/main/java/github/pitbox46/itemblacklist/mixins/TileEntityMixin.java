package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class TileEntityMixin implements ICapabilityProvider {
    @Shadow @Nullable protected Level level;

    @Inject(at = @At(value = "HEAD"), method = "setChanged()V")
    public void onMarkDirty(CallbackInfo ci) {
        if(level != null) {
            if (this instanceof Container) {
                for (int i = 0; i < ((Container) this).getContainerSize(); i++) {
                    if (ItemBlacklist.shouldDelete(((Container) this).getItem(i))) {
                        ((Container) this).setItem(i, ItemStack.EMPTY);
                    }
                }
            } else {
                this.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(cap -> {
                    if (cap instanceof IItemHandlerModifiable) {
                        for (int i = 0; i < cap.getSlots(); i++) {
                            if (ItemBlacklist.shouldDelete(cap.getStackInSlot(i))) {
                                ((IItemHandlerModifiable) cap).setStackInSlot(i, ItemStack.EMPTY);
                            }
                        }
                    }
                });
            }
        }
    }
}
