package github.pitbox46.itemblacklist.commands;


import com.mojang.brigadier.CommandDispatcher;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.blacklist.Blacklist;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.registries.ForgeRegistries;

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
                            Blacklist.MASTER_CALC_VER++;
                            return 0;
                        }))
        );
    }
}