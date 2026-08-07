package com.JSUSHDX.WorldTriggerMod;

import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.JSUSHDX.WorldTriggerMod.util.TrionUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class WorldTriggerModCustomCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wt")
                        .then(Commands.literal("trion")
                                .then(Commands.literal("get")
                                        .executes(context -> executeTrionGet(context.getSource()))
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", com.mojang.brigadier.arguments.FloatArgumentType.floatArg())
                                                .executes(context -> executeTrionAdd(context.getSource(), com.mojang.brigadier.arguments.FloatArgumentType.getFloat(context, "amount")))
                                        )
                                )
                                .then(Commands.literal("consume")
                                        .then(Commands.argument("amount", com.mojang.brigadier.arguments.FloatArgumentType.floatArg())
                                                .executes(context -> executeTrionConsume(context.getSource(), com.mojang.brigadier.arguments.FloatArgumentType.getFloat(context, "amount")))
                                        )
                                )
                        )
        );
    }

    public static int executeTrionGet(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            float trion = TrionUtils.getTrion(player);
            float maxTrion = TrionUtils.getMaxTrion(player);
            player.sendSystemMessage(Component.literal("§b[WT] 當前觸力能: " + trion + " / " + maxTrion));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
    }

    public static int executeTrionAdd(CommandSourceStack source, float amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            TrionUtils.addTrion(player, amount);
            float current = TrionUtils.getTrion(player);
            player.sendSystemMessage(Component.literal("§a[WT] 增加 " + amount + " 觸力能。當前: " + current));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
    }

    public static int executeTrionConsume(CommandSourceStack source, float amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            boolean success = TrionUtils.consumeTrion(player, amount);
            if (success) {
                float current = TrionUtils.getTrion(player);
                player.sendSystemMessage(Component.literal("§e[WT] 成功消耗 " + amount + " 觸力能。剩餘: " + current));
                return 1;
            } else {
                float current = TrionUtils.getTrion(player);
                player.sendSystemMessage(Component.literal("§c[WT] 觸力能不足！需要: " + amount + "，目前只有: " + current));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
    }
}
