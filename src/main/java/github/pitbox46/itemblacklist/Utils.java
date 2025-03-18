package github.pitbox46.itemblacklist;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class Utils {
    public static void broadcastMessage(MinecraftServer server, Component component) {
        server.sendSystemMessage(component);
        for (ServerPlayer serverplayer : server.getPlayerList().getPlayers()) {
            if (Config.SHOW_MESSAGES.getAsBoolean() || serverplayer.hasPermissions(1)) {
                serverplayer.sendSystemMessage(component, false);
            }
        }
    }
}
