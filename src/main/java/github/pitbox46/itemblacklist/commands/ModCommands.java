package github.pitbox46.itemblacklist.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(CommandBanItem.register(dispatcher))
                        .then(CommandUnbanItem.register(dispatcher))
                        .then(CommandBanList.register(dispatcher))
                        .then(Commands.literal("hand")
                                .executes(context -> hand(context.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND))
                        )
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }

    private static int hand(ServerPlayer player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var stackcount = stack.getCount();

        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, player.getItemInHand(hand).getItem());
        player.sendMessage(new TextComponent("Item banned: ").append(stack.getItem().getRegistryName().toString()), ChatType.CHAT, Util.NIL_UUID);
        player.getItemInHand(hand).setCount(stackcount);

        return 0;
    }
}
