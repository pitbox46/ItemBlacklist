package github.pitbox46.itemblacklist;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.blacklist.Blacklist;
import github.pitbox46.itemblacklist.blacklist.ItemBanPredicate;
import github.pitbox46.itemblacklist.commands.ModCommands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Mod("itemblacklist")
public class ItemBlacklist {
    public static final Logger LOGGER = LogManager.getLogger();
    public static File BLACKLIST_FILE = null;
    public static Blacklist BLACKLIST = null;
    /**
     * Modified version of JsonOps that is null safe
     */
    public static JsonOps MODIFIED_JSON_OPS = new JsonOps(false) {
        @Override
        public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
            if (input == null) {
                return outOps.empty();
            }
            return super.convertTo(outOps, input);
        }
    };

    public ItemBlacklist() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Path modFolder = event.getServer().getWorldPath(new LevelResource("serverconfig"));
        BLACKLIST_FILE = JsonUtils.initialize(modFolder, "itemblacklist.json", event.getServer().registryAccess());
        BLACKLIST = JsonUtils.readFromJson(BLACKLIST_FILE, event.getServer().registryAccess());
    }

    @SubscribeEvent
    public void onServerSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof Level level &&
                level.dimension() == level.getServer().overworld().dimension()
        ) {
            JsonUtils.writeJson(BLACKLIST_FILE, BLACKLIST, event.getLevel().registryAccess());
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof ItemEntity itemEntity) {
            if(shouldDelete(itemEntity.getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        if(shouldDelete(event.getItem().getItem(), event.getEntity())) {
            event.setCanceled(true);
            event.getItem().remove(Entity.RemovalReason.KILLED);
        }
    }

    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent event) {
        for(int i = 0; i < event.getContainer().slots.size(); ++i) {
            if(shouldDelete(event.getContainer().getSlot(i).getItem(), event.getEntity())) {
                event.getContainer().getSlot(i).set(ItemStack.EMPTY);
            }
        }
    }

    public static boolean shouldDelete(ItemStack stack) {
        return shouldDelete(stack, null);
    }

    public static boolean shouldDelete(ItemStack stack, @Nullable Player player) {
        BanItemEvent event = new BanItemEvent(stack);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getResult() == Event.Result.DENY) {
            return true;
        }
        else {
            return BLACKLIST.shouldBan(stack, player);
        }
    }

    public static String itemListToString(Collection<ItemBanPredicate> itemList) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(ItemBanPredicate pred: itemList) {
            builder.append(pred.itemPredicate().items).append(", ");
        }
        if(!itemList.isEmpty()) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(']');
        return builder.toString();
    }
}
