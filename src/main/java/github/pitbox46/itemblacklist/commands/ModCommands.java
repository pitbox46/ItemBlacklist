package github.pitbox46.itemblacklist.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(CommandBanItem.register(dispatcher))
                        .then(CommandUnbanItem.register(dispatcher))
                        .then(CommandBanList.register(dispatcher))
                        .then(Commands.literal("hand")
                                .requires(cs -> cs.hasPermission(2))
                                .executes(ModCommands::hand)
                        )
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }

    private static int hand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ItemStack bannedItem = context.getSource().getPlayerOrException().getMainHandItem();

        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, bannedItem.getItem());
        PlayerList playerList = context.getSource().getServer().getPlayerList();
        playerList.broadcastMessage(new TextComponent("Item banned: ").append(bannedItem.getItem().getRegistryName().toString()), ChatType.CHAT, Util.NIL_UUID);
        for(ServerPlayer player : playerList.getPlayers()) {
            for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if(ItemBlacklist.shouldDelete(player.getInventory().getItem(i)))
                    player.getInventory().getItem(i).setCount(0);
            }
        }
        return 0;
    }
}
