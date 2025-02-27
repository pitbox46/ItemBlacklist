package github.pitbox46.itemblacklist.blacklist;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class ItemBanPredicate implements BiPredicate<ItemStack, Player> {
    public static final Codec<ItemBanPredicate> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.CODEC
                            .fieldOf("item_predicate")
                            .forGetter(ItemBanPredicate::predicateStack),
                    Codec.STRING.listOf()
                            .optionalFieldOf("groups", new ArrayList<>())
                            .xmap(ArrayList::new, ArrayList::new).forGetter(ItemBanPredicate::groups)
            ).apply(instance, ItemBanPredicate::new)
    );
    private int calcVer = 0;
    private final ItemStack predicateStack;
    private final ArrayList<String> groupKeys;

    private final List<Group> groups = new ArrayList<>();
    private final Object2BooleanMap<Player> cachedPlayers = new Object2BooleanOpenHashMap<>();

    public ItemBanPredicate(ItemStack predicateStack, ArrayList<String> groupKeys) {
        this.predicateStack = predicateStack;
        this.groupKeys = groupKeys;
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
        if (!predicateStack.is(stack.getItem())) {
            return false;
        }

        if (player == null) {
            return testItemStack(stack);
        }

        //Reset the cache if the master version tells us to
        if (Blacklist.MASTER_CALC_VER > calcVer) {
            cachedPlayers.clear();
            calcVer = Blacklist.MASTER_CALC_VER;
        }

        if (cachedPlayers.computeIfAbsent(
                player,
                (Predicate<? super Player>) p -> groups.stream().anyMatch(group -> group.test(player))
        )) {
            return testItemStack(stack);
        }
        return false;
    }

    public boolean testItemStack(ItemStack stack) {
        if (predicateStack.is(stack.getItem())) {
            return predicateStack.getComponentsPatch()
                    .entrySet()
                    .stream()
                    .allMatch(entry -> {
                        Optional<?> value = entry.getValue();
                        if (value.isPresent()) {
                            Optional<?> value2 = stack.getComponentsPatch().get(entry.getKey());
                            if (value == null || value2 == null) {
                                return false;
                            }
                            return Objects.equals(value.orElse(null), value2.orElse(null));
                        }
                        return false;
                    });
        }
        return false;
    }

    //region Record Boilerplate

    public ItemStack predicateStack() {
        return predicateStack;
    }

    public ArrayList<String> groups() {
        return groupKeys;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemBanPredicate) obj;
        return Objects.equals(this.predicateStack, that.predicateStack) &&
                Objects.equals(this.groupKeys, that.groupKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicateStack, groupKeys);
    }

    @Override
    public String toString() {
        return "ItemBanPredicate[" +
                "itemPredicate=" + predicateStack + ", " +
                "groups=" + groupKeys + ']';
    }
    //endregion
}
