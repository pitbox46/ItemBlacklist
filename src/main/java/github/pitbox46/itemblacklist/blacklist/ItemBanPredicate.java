package github.pitbox46.itemblacklist.blacklist;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class ItemBanPredicate implements BiPredicate<ItemStack, Player> {
    public static final Codec<ItemBanPredicate> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemPredicate.CODEC
                            .fieldOf("item_predicate")
                            .forGetter(ItemBanPredicate::itemPredicate),
                    Codec.STRING.listOf()
                            .optionalFieldOf("groups", new ArrayList<>())
                            .xmap(ArrayList::new, ArrayList::new)
                            .forGetter(ItemBanPredicate::groupKeys),
                    Codec.BOOL
                            .optionalFieldOf("ban_nonplayer", true)
                            .forGetter(ItemBanPredicate::banNonPlayerItems)
            ).apply(instance, ItemBanPredicate::new)
    );
    private final ItemPredicate itemPredicate;
    private final ArrayList<String> groupKeys;
    private final boolean banNonPlayerItems;

    private final Object2BooleanMap<Player> cachedPlayers = new Object2BooleanOpenHashMap<>();

    public ItemBanPredicate(ItemPredicate itemPredicate, ArrayList<String> groupKeys, boolean banNonPlayerItems) {
        this.itemPredicate = itemPredicate;
        this.groupKeys = groupKeys;
        this.banNonPlayerItems = banNonPlayerItems;
    }

    @Override
    public boolean test(ItemStack stack, @Nullable Player player) {
        if (player == null) {
            return banNonPlayerItems && itemPredicate.test(stack);
        }

        if (cachedPlayers.computeIfAbsent(
                player,
                (Predicate<? super Player>) p -> ItemBlacklist.BLACKLIST.isPlayerInGroups(groupKeys, p)
        )) {
            return itemPredicate().test(stack);
        }
        return false;
    }

    public void recalculate() {
        cachedPlayers.clear();
    }

    //region Record Boilerplate
    public ItemPredicate itemPredicate() {
        return itemPredicate;
    }

    public ArrayList<String> groupKeys() {
        return groupKeys;
    }

    public boolean banNonPlayerItems() {
        return banNonPlayerItems;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemBanPredicate) obj;
        return Objects.equals(this.itemPredicate, that.itemPredicate) &&
                Objects.equals(this.groupKeys, that.groupKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPredicate, groupKeys);
    }

    @Override
    public String toString() {
        return "ItemBanPredicate[" +
                "itemPredicate=" + itemPredicate + ", " +
                "groups=" + groupKeys + ']';
    }
    //endregion
}
