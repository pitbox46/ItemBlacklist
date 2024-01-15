package github.pitbox46.itemblacklist;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Fired whenever an item stack is looked at for deletion
 */
public class BanItemEvent extends Event {
    public final ItemStack stack;
    public boolean deleteItem = false;

    public BanItemEvent(ItemStack stack) {
        this.stack = stack;
    }
}
