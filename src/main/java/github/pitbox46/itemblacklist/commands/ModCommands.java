package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
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
        // Below Line Removes the item from hand when blacklisting is succesful - kept out for those who wouldn't care for it unless a config option was a thing.
        //player.getItemInHand(hand).setCount(stackcount);

        return 0;
    }
}