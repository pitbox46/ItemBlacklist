package github.pitbox46.itemblacklist;

import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;

public class Utils {
    public static void broadcastMessage(MinecraftServer server, Component component) {
        server.getPlayerList().broadcastChatMessage(
                PlayerChatMessage.unsigned(component),
                ChatSender.system(Component.literal("Item Blacklist")),
                ChatType.CHAT
        );
    }
}
