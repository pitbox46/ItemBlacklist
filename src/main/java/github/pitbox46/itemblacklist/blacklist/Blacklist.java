package github.pitbox46.itemblacklist.blacklist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
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

public record Blacklist(ArrayList<ItemBanPredicate> bannedItems, ArrayList<Group> groups) {
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
                            .xmap(ArrayList::new, ArrayList::new)
                            .forGetter(Blacklist::groups)
            ).apply(instance, Blacklist::new)
    );

    public static int MASTER_CALC_VER = 0;

    public boolean shouldBan(ItemStack stack, @Nullable Player player) {
        for (var pred : bannedItems) {
            if (pred.test(stack, player)) return true;
        }
        return false;
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
     * @param group The group
     */
    public void addItem(ItemStack stack, String group) {
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
            matchingPred.get().groups().add(group);
            matchingPred.get().mapGroups(groups);
        } else {
            ItemBanPredicate pred = new ItemBanPredicate(itemPredicate, Util.make(new ArrayList<>(), l -> l.add(group)));
            bannedItems.add(pred);
            pred.mapGroups(groups);
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

    public static Blacklist emptyBlacklist() {
        return new Blacklist(
                new ArrayList<>(),
                Util.make(new ArrayList<>(), l -> l.add(new Group("default", Group.Properties.EMPTY)))
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
        blacklist.bannedItems().forEach(pred -> pred.mapGroups(blacklist.groups()));
        return blacklist;
    }
    //endregion
}
