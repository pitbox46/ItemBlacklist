package github.pitbox46.itemblacklist.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(CommandBanItem.register(dispatcher))
                        .then(CommandUnbanItem.register(dispatcher))
                        .then(CommandBanList.register(dispatcher))
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }
}
