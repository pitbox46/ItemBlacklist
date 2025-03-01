package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CommandUnbanItem {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("unban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands
                        .argument("item", ItemArgument.item(context))
                        .executes(ctx -> {
                            try {
                                ItemInput itemInput = ItemArgument.getItem(ctx, "item");
                                ItemStack stack = itemInput.createItemStack(1, false);
                                ItemBlacklist.BLACKLIST.searchAndRemove(stack);

                                Utils.broadcastMessage(ctx.getSource().getServer(),
                                        Component.literal("Item unbanned: ")
                                                .append(ItemArgument.getItem(ctx, "item").getItem().toString()));
                            } catch(IndexOutOfBoundsException e) {
                                ctx.getSource().sendFailure(Component.literal("The item could not be unbanned."));
                            }
                            return 0;
                        }))
                .then(Commands
                        .literal("all")
                        .executes(ctx -> {
                            ItemBlacklist.BLACKLIST.bannedItems().clear();
                            Utils.broadcastMessage(ctx.getSource().getServer(), Component.literal("All items unbanned"));
                            return 0;
                        }));
    }
}