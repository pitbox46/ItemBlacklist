package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import java.util.Objects;
import java.util.UUID;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(CommandBanItem.register(dispatcher))
                        .then(CommandUnbanItem.register(dispatcher))
                        .then(CommandBanList.register(dispatcher))
                        .then(Commands.literal("hand")
                                .executes(context -> hand(context.getSource().asPlayer()))
                        )
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }

    private static int hand(ServerPlayerEntity player) {
        ItemStack stack = player.getHeldItemMainhand();
        int stackcount = stack.getCount();

        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, stack.getItem());
        player.sendMessage(new StringTextComponent("Item banned: ").appendString(Objects.requireNonNull(stack.getItem().getRegistryName()).toString()), UUID.randomUUID());

        stack.setCount(-stackcount);

        return 0;
    }
}