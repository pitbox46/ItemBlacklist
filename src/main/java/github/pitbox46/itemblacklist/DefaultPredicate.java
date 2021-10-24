package github.pitbox46.itemblacklist;

import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class DefaultPredicate implements Predicate<ItemStack> {
    @Override
    public boolean test(ItemStack itemStack) {
        return ItemBlacklist.BANNED_ITEMS.contains(itemStack.getItem());
    }

    @Override
    public Predicate<ItemStack> or(Predicate<? super ItemStack> other) {
        return ((Predicate<ItemStack>) itemStack -> ItemBlacklist.BANNED_ITEMS.contains(itemStack.getItem())).or(other);
    }
}
