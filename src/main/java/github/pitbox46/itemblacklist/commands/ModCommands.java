package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(CommandBanItem.register(dispatcher))
                        .then(CommandUnbanItem.register(dispatcher))
                        .then(CommandBanList.register(dispatcher))
                        .then(Commands.literal("hand")
                                .requires(cs -> cs.hasPermissionLevel(2))
                                .executes(ModCommands::hand)
                        )
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }

    private static int hand(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ItemStack bannedItem = context.getSource().asPlayer().getHeldItemMainhand();

        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, bannedItem.getItem());
        PlayerList playerList = context.getSource().getServer().getPlayerList();
        playerList.func_232641_a_(new StringTextComponent("Item banned: ").appendString(bannedItem.getItem().getRegistryName().toString()), ChatType.CHAT, Util.DUMMY_UUID);
        for(ServerPlayerEntity player : playerList.getPlayers()) {
            for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if(ItemBlacklist.shouldDelete(stack))
                    stack.setCount(0);
            }
        }
        return 0;
    }
}