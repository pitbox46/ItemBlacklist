package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("itemblacklist")
                .then(CommandBanItem.register(dispatcher, context))
                .then(CommandUnbanItem.register(dispatcher, context))
                .then(Commands
                        .literal("list")
                        .executes(ctx -> {
                            ctx.getSource().getPlayerOrException().displayClientMessage(
                                    Component.literal("Items banned: ")
                                            .append(ItemBlacklist.itemListToString(ItemBlacklist.BLACKLIST.bannedItems())),
                                    false);
                            return 0;
                        }))
                .then(Commands
                        .literal("recalculate")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(ctx -> {
                            ItemBlacklist.BLACKLIST.recalculate();
                            return 0;
                        }))
        );
    }
}