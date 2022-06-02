package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

public class CommandUnbanItem implements Command<CommandSource> {
    private static final CommandUnbanItem CMD = new CommandUnbanItem();
    private static final UnbanAll CMD_ALL = new UnbanAll();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("unban")
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("item", ItemArgument.item())
                        .executes(CMD))
                .then(Commands.literal("all")
                        .executes(CMD_ALL));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        try {
            JsonUtils.removeItemFromJson(ItemBlacklist.BANLIST, ItemArgument.getItem(context, "item").getItem());
            context.getSource().getServer().getPlayerList().func_232641_a_(new StringTextComponent("Item unbanned: ").appendString(ItemArgument.getItem(context, "item").getItem().getRegistryName().toString()), ChatType.CHAT, Util.DUMMY_UUID);
        } catch(IndexOutOfBoundsException e) {
            context.getSource().sendErrorMessage(new StringTextComponent("The item could not be unbanned."));
        }
        return 0;
    }

    public static class UnbanAll implements Command<CommandSource>{
        @Override
        public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
            JsonUtils.removeAllItemsFromJson(ItemBlacklist.BANLIST);
            context.getSource().getServer().getPlayerList().func_232641_a_(new StringTextComponent("All items unbanned"), ChatType.CHAT, Util.DUMMY_UUID);
            return 0;
        }
    }
}
