package github.pitbox46.itemblacklist.blacklist;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class ItemBanPredicate implements BiPredicate<ItemStack, Player> {
    public static final Codec<ItemBanPredicate> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.PASSTHROUGH
                            .xmap(
                                    (p_253507_) -> p_253507_.convert(ItemBlacklist.MODIFIED_JSON_OPS).getValue(),
                                    (p_253513_) -> new Dynamic<>(ItemBlacklist.MODIFIED_JSON_OPS, p_253513_)
                            )
                            .fieldOf("item_predicate")
                            .xmap(ItemPredicate::fromJson, ItemPredicate::serializeToJson)
                            .forGetter(ItemBanPredicate::itemPredicate),
                    Codec.STRING.listOf()
                            .optionalFieldOf("groups", new ArrayList<>())
                            .xmap(ArrayList::new, ArrayList::new).forGetter(ItemBanPredicate::groups)
            ).apply(instance, ItemBanPredicate::new)
    );
    private int calcVer = 0;
    private final ItemPredicate itemPredicate;
    private final ArrayList<String> groupKeys;
    private final boolean banNonPlayerItems;

    private final List<Group> groups = new ArrayList<>();
    private final Object2BooleanMap<Player> cachedPlayers = new Object2BooleanOpenHashMap<>();

    public ItemBanPredicate(ItemPredicate itemPredicate, ArrayList<String> groupKeys, boolean banNonPlayerItems) {
        this.itemPredicate = itemPredicate;
        this.groupKeys = groupKeys;
        this.banNonPlayerItems = banNonPlayerItems;
    }

    /**
     * Finds a group corresponding to each key.
     * Fired after Blacklist is read in
     * @param masterGroupList The list of groups from {@link Blacklist#groups()}
     */
    public void mapGroups(List<Group> masterGroupList) {
        masterGroupList.stream()
                .filter(group -> groupKeys.contains(group.name()))
                .collect(() -> groups, List::add, List::addAll);
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
                Objects.equals(this.groupKeys, that.groupKeys) &&
                this.banNonPlayerItems == that.banNonPlayerItems;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemPredicate, groupKeys, banNonPlayerItems);
    }

    @Override
    public String toString() {
        return "ItemBanPredicate[" +
                "itemPredicate=" + itemPredicate + ", " +
                "groups=" + groupKeys +
                "banNonPlayerItems=" + banNonPlayerItems +
                ']';
    }
    //endregion
}
