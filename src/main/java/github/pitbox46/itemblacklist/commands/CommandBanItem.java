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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

public class CommandBanItem implements Command<CommandSource> {
    private static final CommandBanItem CMD = new CommandBanItem();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("ban")
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("item", ItemArgument.item())
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, ItemArgument.getItem(context, "item").getItem());
        PlayerList playerList = context.getSource().getServer().getPlayerList();
        playerList.func_232641_a_(new StringTextComponent("Item banned: ").appendString(ItemArgument.getItem(context, "item").getItem().getRegistryName().toString()), ChatType.CHAT, Util.DUMMY_UUID);
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
