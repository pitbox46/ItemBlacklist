package github.pitbox46.itemblacklist;

import github.pitbox46.itemblacklist.commands.ModCommands;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
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

    public ItemBlacklist() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        Path modFolder = event.getServer().func_240776_a_(new FolderName("serverconfig"));
        BANLIST = JsonUtils.initialize(modFolder, "serverconfig", "itemblacklist.json");
        BANNED_ITEMS = JsonUtils.readItemsFromJson(BANLIST);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof ItemEntity) {
            if(shouldDelete(((ItemEntity) event.getEntity()).getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if(shouldDelete(event.getStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerContainerEvent(PlayerContainerEvent event) {
        for(int i = 0; i < event.getContainer().inventorySlots.size(); ++i) {
            if(shouldDelete(event.getContainer().getInventory().get(i))) {
                event.getContainer().getInventory().set(i, ItemStack.EMPTY);
            }
        }
    }

    public static boolean shouldDelete(ItemStack stack) {
        BanItemEvent event = new BanItemEvent(stack);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.getResult() == Event.Result.DEFAULT) return BANNED_ITEMS.contains(stack.getItem());
        else return event.getResult() == Event.Result.DENY;
    }

    public static String itemListToString(List<Item> itemList) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for(Item item: itemList) {
            builder.append(item.getRegistryName().toString()).append(", ");
        }
        if(itemList.size() > 0) builder.delete(builder.length() - 2, builder.length());
        builder.append(']');
        return builder.toString();
    }
}
