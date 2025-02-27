package github.pitbox46.itemblacklist.blacklist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.Util;
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
     * Simple function that bans a singular item. We use the default group
     */
    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemBanPredicate pred = new ItemBanPredicate(stack, Util.make(new ArrayList<>(), l -> l.add("default")));
        bannedItems.add(pred);
        pred.mapGroups(groups);
    }

    /**
     * Removes any ban whose item predicate matches the stack
     * @param stack The itemstack to search for
     * @return If any bans were removed
     */
    public boolean searchAndRemove(ItemStack stack) {
        return bannedItems.removeIf(pred -> pred.testItemStack(stack));
    }

    //region serialization
    public JsonElement encodeToJSON() {
        var encoded = Blacklist.CODEC.encodeStart(JsonOps.INSTANCE, this);
        return encoded.result().orElseThrow();
    }

    public static Blacklist readBlacklist(JsonObject json) {
        Blacklist blacklist = CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(m -> ItemBlacklist.LOGGER.warn("Could not read blacklist: {}", m))
                .orElseGet(() -> new Blacklist(new ArrayList<>(), new ArrayList<>()));
        blacklist.bannedItems().forEach(pred -> pred.mapGroups(blacklist.groups()));
        return blacklist;
    }
    //endregion
}
