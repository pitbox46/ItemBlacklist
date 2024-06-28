package github.pitbox46.itemblacklist;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class Utils {
    public static void broadcastMessage(MinecraftServer server, Component component) {
        server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(component));
    }
}
