package github.pitbox46.itemblacklist.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import github.pitbox46.itemblacklist.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(CommandBanItem.register(dispatcher, context))
                        .then(CommandUnbanItem.register(dispatcher, context))
                        .then(CommandBanList.register(dispatcher, context))
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }

    private static int hand(CommandContext<CommandSourceStack> context, InteractionHand hand) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        var stack = player.getItemInHand(hand);
        var stackcount = stack.getCount();

        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, player.getItemInHand(hand).getItem());
        Utils.broadcastMessage(context.getSource().getServer(),
                Component.literal("Item banned: ")
                        .append(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()));
        player.getItemInHand(hand).setCount(-stackcount);

        return 0;
    }
}
