package github.pitbox46.itemblacklist.blacklist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public record Blacklist(List<ItemBanPredicate> bannedItems, List<Group> groups) {
    public static final Codec<Blacklist> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemBanPredicate.CODEC.listOf().optionalFieldOf("items", new ArrayList<>()).forGetter(Blacklist::bannedItems),
                    Group.CODEC.listOf().optionalFieldOf("groups", new ArrayList<>()).forGetter(Blacklist::groups)
            ).apply(instance, Blacklist::new)
    );

    public static int MASTER_CALC_VER = 0;

    public static Blacklist readBlacklist(JsonObject json) {
        Blacklist blacklist = CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(m -> ItemBlacklist.LOGGER.warn("Could not read blacklist: {}", m))
                .orElseGet(() -> new Blacklist(new ArrayList<>(), new ArrayList<>()));
        blacklist.bannedItems().forEach(pred -> pred.mapGroups(blacklist.groups()));
        return blacklist;
    }

    public boolean shouldBan(ItemStack stack, @Nullable Player player) {
        for (var pred : bannedItems) {
            if (pred.test(stack, player)) return true;
        }
        return false;
    }

    public JsonElement encodeToJSON() {
        var encoded = Blacklist.CODEC.encodeStart(JsonOps.INSTANCE, this);
        return encoded.result().orElseThrow();
    }
}
