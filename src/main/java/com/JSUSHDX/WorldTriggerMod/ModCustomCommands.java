package com.JSUSHDX.WorldTriggerMod;

import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ModCustomCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wt")
                        .then(Commands.literal("trigger")
                                .then(Commands.literal("on")
                                        .executes(context -> executeTrigger(context.getSource(), true))
                                )
                                .then(Commands.literal("off")
                                        .executes(context -> executeTrigger(context.getSource(), false))
                                )
                        )
        );
    }

    public static int executeTrigger(CommandSourceStack source, Boolean enable) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Can only be execute by player"));
            return 0;
        }

        if (!enable) {
            player.sendSystemMessage(Component.literal("§a[WT] " + player.getPlainTextName() + " Trigger off!"));
            return 1;
        }

        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.is(ModItems.TRIGGER.get())) {
            player.sendSystemMessage(Component.literal("§a[WT] " + player.getPlainTextName() + " Trigger on!"));
            return 1;
        } else {
            player.sendSystemMessage(Component.literal("§c[WT] " + player.getPlainTextName() + " fail to use Trigger."));
            return 0;
        }
    }
}
