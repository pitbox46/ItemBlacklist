package github.pitbox46.itemblacklist.blacklist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public record Blacklist(ArrayList<ItemBanPredicate> bannedItems, HashMap<String, Group> groups) {
    public static final Codec<Blacklist> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemBanPredicate.CODEC.listOf()
                            .fieldOf("items")
                            .orElse(new ArrayList<>())
                            .xmap(ArrayList::new, ArrayList::new)
                            .forGetter(Blacklist::bannedItems),
                    Group.CODEC.listOf()
                            .fieldOf("groups")
                            .orElse(new ArrayList<>())
                            .xmap(
                                    l -> Util.make(
                                            new HashMap<String, Group>(l.size()),
                                            map -> l.forEach(group -> map.put(group.name(), group))
                                    ),
                                    map -> new ArrayList<>(map.values())
                            )
                            .forGetter(Blacklist::groups)
            ).apply(instance, Blacklist::new)
    );

    public boolean shouldBan(ItemStack stack, @Nullable Player player) {
        for (var pred : bannedItems) {
            if (pred.test(stack, player)) return true;
        }
        return false;
    }

    public boolean isPlayerInGroups(Collection<String> groupKeys, Player player) {
        return groupKeys.stream().anyMatch(key -> {
            Group group = groups.get(key);
            return group != null && group.test(player);
        });
    }

    /**
     * Bans an item stack. We use the default group
     */
    public void addItem(ItemStack stack) {
        addItem(stack, "default");
    }


    /**
     * Bans an item stack. Uses the component patch if it exists
     * @param stack The itemstack
     * @param groupKey The group
     */
    public void addItem(ItemStack stack, String groupKey) {
        if (stack.isEmpty()) {
            return;
        }
        ItemPredicate itemPredicate = ItemPredicate.Builder
                .item()
                .of(stack.getItem())
                .hasNbt(stack.getTag())
                .build();
        var matchingPred = bannedItems.stream().filter(pred -> itemPredicate.equals(pred.itemPredicate())).findAny();
        if (matchingPred.isPresent()) {
            matchingPred.get().groupKeys().add(groupKey);
        } else {
            ItemBanPredicate pred = new ItemBanPredicate(
                    itemPredicate,
                    Util.make(new ArrayList<>(), l -> l.add(groupKey)),
                    true
            );
            bannedItems.add(pred);
        }
    }

    /**
     * Removes any ban whose item predicate matches the stack
     * @param stack The itemstack to search for
     * @return If any bans were removed
     */
    public boolean searchAndRemove(ItemStack stack) {
        return bannedItems.removeIf(pred -> pred.itemPredicate().matches(stack));
    }

    public void recalculate() {
        bannedItems.forEach(ItemBanPredicate::recalculate);
    }

    public static Blacklist emptyBlacklist() {
        return new Blacklist(
                new ArrayList<>(),
                Util.make(
                        new HashMap<>(),
                        map -> map.put("default", new Group("default", Group.Properties.EMPTY))
                )
        );
    }

    //region serialization
    public JsonElement encodeToJSON(HolderLookup.Provider levelRegistryAccess) {
        var encoded = Blacklist.CODEC.encodeStart(RegistryOps.create(ItemBlacklist.MODIFIED_JSON_OPS, levelRegistryAccess), this);
        return encoded.result().orElseThrow();
    }

    public static Blacklist readBlacklist(HolderLookup.Provider levelRegistryAccess, JsonObject json) {
        Blacklist blacklist = CODEC.parse(RegistryOps.create(ItemBlacklist.MODIFIED_JSON_OPS, levelRegistryAccess), json)
                .resultOrPartial(m -> ItemBlacklist.LOGGER.warn("Could not read blacklist: {}", m))
                .orElseGet(Blacklist::emptyBlacklist);
        return blacklist;
    }
    //endregion
}
