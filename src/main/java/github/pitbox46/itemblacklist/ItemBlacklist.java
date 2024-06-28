package github.pitbox46.itemblacklist;

import github.pitbox46.itemblacklist.commands.ModCommands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod("itemblacklist")
public class ItemBlacklist {
    private static final Logger LOGGER = LogManager.getLogger();
    public static File BANLIST;
    public static List<Item> BANNED_ITEMS = new ArrayList<>();

    public ItemBlacklist(ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Path modFolder = event.getServer().getWorldPath(new LevelResource("serverconfig"));
        BANLIST = JsonUtils.initialize(modFolder, "serverconfig", "itemblacklist.json");
        BANNED_ITEMS = JsonUtils.readItemsFromJson(BANLIST);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof ItemEntity) {
            if(shouldDelete(((ItemEntity) event.getEntity()).getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if(shouldDelete(event.getItemEntity().getItem())) {
            event.getItemEntity().kill();
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent event) {
        for(int i = 0; i < event.getContainer().slots.size(); ++i) {
            if(shouldDelete(event.getContainer().getSlot(i).getItem())) {
                event.getContainer().getSlot(i).set(ItemStack.EMPTY);
            }
        }
    }

    public static boolean shouldDelete(ItemStack stack) {
        BanItemEvent event = new BanItemEvent(stack);
        NeoForge.EVENT_BUS.post(event);
        if(event.deleteItem) {
            return true;
        }
        else {
            return BANNED_ITEMS.contains(stack.getItem());
        }
    }

    public static String itemListToString(List<Item> itemList) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(Item item: itemList) {
            builder.append(BuiltInRegistries.ITEM.getKey(item)).append(", ");
        }
        if(!itemList.isEmpty()) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(']');
        return builder.toString();
    }
}
