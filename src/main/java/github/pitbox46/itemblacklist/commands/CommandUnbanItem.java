package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

public class CommandUnbanItem implements Command<CommandSourceStack> {
    private static final CommandUnbanItem CMD = new CommandUnbanItem();
    private static final UnbanAll CMD_ALL = new UnbanAll();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("unban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item())
                        .executes(CMD))
                .then(Commands.literal("all")
                        .executes(CMD_ALL));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            JsonUtils.removeItemFromJson(ItemBlacklist.BANLIST, ItemArgument.getItem(context, "item").getItem());
            context.getSource().getServer().getPlayerList().broadcastMessage(new TextComponent("Item unbanned: ").append(ItemArgument.getItem(context, "item").getItem().getRegistryName().toString()), ChatType.CHAT, Util.NIL_UUID);
        } catch(IndexOutOfBoundsException e) {
            context.getSource().sendFailure(new TextComponent("The item could not be unbanned."));
        }
        return 0;
    }

    public static class UnbanAll implements Command<CommandSourceStack>{
        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            JsonUtils.removeAllItemsFromJson(ItemBlacklist.BANLIST);
            context.getSource().getServer().getPlayerList().broadcastMessage(new TextComponent("All items unbanned"), ChatType.CHAT, Util.NIL_UUID);
            return 0;
        }
    }
}
