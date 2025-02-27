package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.Utils;
import github.pitbox46.itemblacklist.blacklist.Blacklist;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;

public class CommandBanItem {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("ban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(context))
                        .executes(ctx -> banItem(
                                ctx,
                                ItemArgument.getItem(ctx, "item").createItemStack(1, false),
                                "default")
                        )
                        .then(Commands.argument("group", StringArgumentType.word())
                                .executes(ctx -> banItem(
                                        ctx,
                                        ItemArgument.getItem(ctx, "item").createItemStack(1, false),
                                        StringArgumentType.getString(ctx, "group")))
                        )
                )
                .then(Commands.literal("hand")
                        .executes(ctx -> {
                            ItemStack stack = ctx.getSource().getPlayerOrException().getMainHandItem();
                            return banItem(ctx, stack, "default");
                        }).then(Commands.argument("group", StringArgumentType.word())
                                .executes(ctx -> banItem(
                                        ctx,
                                        ctx.getSource().getPlayerOrException().getMainHandItem(),
                                        StringArgumentType.getString(ctx, "group")))
                        )
                );
    }

    private static int banItem(CommandContext<CommandSourceStack> context, ItemStack stack, String group) {
        if(stack.isEmpty())
            return 1;
        ItemBlacklist.BLACKLIST.addItem(stack.copy(), group);

        PlayerList playerList = context.getSource().getServer().getPlayerList();
        Utils.broadcastMessage(context.getSource().getServer(),
                Component.literal("Item banned: ")
                        .append(stack.getItem().toString()));

        for(ServerPlayer player : playerList.getPlayers()) {
            for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stackInSlot = player.getInventory().getItem(i);
                if(ItemBlacklist.shouldDelete(stackInSlot, player))
                    stackInSlot.setCount(0);
            }
        }

        Blacklist.MASTER_CALC_VER++;
        return 0;
    }
}
