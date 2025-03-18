package github.pitbox46.itemblacklist;

import github.pitbox46.itemblacklist.blacklist.Blacklist;
import github.pitbox46.itemblacklist.blacklist.ItemBanPredicate;
import github.pitbox46.itemblacklist.commands.ModCommands;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

@Mod("itemblacklist")
public class ItemBlacklist {
    public static final Logger LOGGER = LogManager.getLogger();
    public static File BLACKLIST_FILE = null;
    public static Blacklist BLACKLIST = Blacklist.emptyBlacklist();

    public ItemBlacklist(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, Config.SERVER, "itemblacklist.properties.toml");
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Path modFolder = event.getServer().getWorldPath(new LevelResource("serverconfig"));
        BLACKLIST_FILE = JsonUtils.initialize(modFolder, "itemblacklist.json", event.getServer().registryAccess());
        BLACKLIST = JsonUtils.readFromJson(BLACKLIST_FILE, event.getServer().registryAccess());
    }

    @SubscribeEvent
    public void onServerSave(LevelEvent.Save event) {
        JsonUtils.writeJson(BLACKLIST_FILE, BLACKLIST, event.getLevel().registryAccess());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (Config.BAN_ITEM_ENTITY.getAsBoolean() && Config.testBanRate()) {
            if (event.getEntity() instanceof ItemEntity) {
                if (shouldDelete(((ItemEntity) event.getEntity()).getItem())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (Config.BAN_ITEM_ENTITY.getAsBoolean() && Config.testBanRate()) {
            if (shouldDelete(event.getItemEntity().getItem(), event.getPlayer())) {
                event.getItemEntity().remove(Entity.RemovalReason.KILLED);
                event.setCanPickup(TriState.FALSE);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent event) {
        if (Config.BAN_CONTAINER.getAsBoolean() && Config.testBanRate()) {
            for (int i = 0; i < event.getContainer().slots.size(); ++i) {
                if (shouldDelete(event.getContainer().getSlot(i).getItem(), event.getEntity())) {
                    event.getContainer().getSlot(i).set(ItemStack.EMPTY);
                }
            }
        }
    }

    public static boolean shouldDelete(ItemStack stack) {
        return shouldDelete(stack, null);
    }

    public static boolean shouldDelete(ItemStack stack, @Nullable Player player) {
        BanItemEvent event = new BanItemEvent(stack);
        NeoForge.EVENT_BUS.post(event);
        if(event.deleteItem) {
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
            builder.append(pred.itemPredicate().items().orElse(HolderSet.empty())).append(", ");
        }
        if(!itemList.isEmpty()) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(']');
        return builder.toString();
    }
}
