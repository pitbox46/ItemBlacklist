package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin extends net.neoforged.neoforge.attachment.AttachmentHolder implements net.neoforged.neoforge.common.extensions.IBlockEntityExtension {
    @Shadow @Nullable protected Level level;

    @Shadow public abstract BlockPos getBlockPos();

    @Shadow private BlockState blockState;

    @Inject(at = @At(value = "HEAD"), method = "setChanged()V")
    public void onMarkDirty(CallbackInfo ci) {
        if(level != null) {
            if (this instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (ItemBlacklist.shouldDelete(stack)) {
                        container.removeItemNoUpdate(i);
                    }
                }
            } else {
                IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, getBlockPos(), blockState, (BlockEntity) (Object) this, Direction.WEST);
                if (cap != null){
                    if (cap instanceof IItemHandlerModifiable) {
                        for (int i = 0; i < cap.getSlots(); i++) {
                            if (ItemBlacklist.shouldDelete(cap.getStackInSlot(i))) {
                                ((IItemHandlerModifiable) cap).setStackInSlot(i, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
        }
    }
}
