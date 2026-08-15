package com.JSUSHDX.WorldTriggerMod.gametest;

import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.custom.MotherTriggerNetwork;
import com.JSUSHDX.WorldTriggerMod.gametest.support.TriggerTestSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * GameTest bodies for {@code RecallBedBlock} binding/unbinding and the trigger's bail-out teleport
 * (Phase B). See {@link WTGameTestFunctions} for why these are plain method references rather than
 * registry entries.
 */
public class WTRecallBedGameTestFunctions {

    private static final BlockPos RECALL_BED_POS = new BlockPos(1, 1, 1);

    public static final Consumer<GameTestHelper> BIND_NEAR_MOTHER_TRIGGER_SETS_RECALL_POS =
            WTRecallBedGameTestFunctions::bindNearMotherTriggerSetsRecallPos;
    public static final Consumer<GameTestHelper> BIND_FAR_FROM_MOTHER_TRIGGER_DOES_NOT_BIND =
            WTRecallBedGameTestFunctions::bindFarFromMotherTriggerDoesNotBind;
    public static final Consumer<GameTestHelper> UNBIND_SHIFT_CLICK_CLEARS_RECALL_POS =
            WTRecallBedGameTestFunctions::unbindShiftClickClearsRecallPos;
    public static final Consumer<GameTestHelper> BAIL_OUT_TELEPORTS_TO_RECALL_POS =
            WTRecallBedGameTestFunctions::bailOutTeleportsToRecallPos;

    private WTRecallBedGameTestFunctions() {
    }

    private static void bindNearMotherTriggerSetsRecallPos(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        helper.setBlock(RECALL_BED_POS, ModBlocks.RECALL_BED.get());
        BlockPos absoluteBedPos = helper.absolutePos(RECALL_BED_POS);
        ServerLevel level = helper.getLevel();

        MotherTriggerNetwork.get(level).register(absoluteBedPos);
        try {
            helper.useBlock(RECALL_BED_POS, player);

            Vec3 recallPos = trigger.get(ModDataComponents.TRIGGER_RECALL_POS);
            helper.assertTrue(recallPos != null, "Recall position should be bound near a Mother Trigger");
            Vec3 expected = Vec3.atCenterOf(absoluteBedPos).add(0, 1, 0);
            helper.assertTrue(recallPos.equals(expected), "Bound recall position should match the recall bed's location");
        } finally {
            MotherTriggerNetwork.get(level).unregister(absoluteBedPos);
        }

        helper.succeed();
    }

    private static void bindFarFromMotherTriggerDoesNotBind(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        helper.setBlock(RECALL_BED_POS, ModBlocks.RECALL_BED.get());

        helper.useBlock(RECALL_BED_POS, player);

        helper.assertTrue(trigger.get(ModDataComponents.TRIGGER_RECALL_POS) == null,
                "Recall position should not be bound when there's no Mother Trigger in range");
        helper.succeed();
    }

    private static void unbindShiftClickClearsRecallPos(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        helper.setBlock(RECALL_BED_POS, ModBlocks.RECALL_BED.get());
        BlockPos absoluteBedPos = helper.absolutePos(RECALL_BED_POS);
        ServerLevel level = helper.getLevel();

        MotherTriggerNetwork.get(level).register(absoluteBedPos);
        try {
            helper.useBlock(RECALL_BED_POS, player);
            helper.assertTrue(trigger.get(ModDataComponents.TRIGGER_RECALL_POS) != null,
                    "Precondition: recall position should be bound before testing unbind");

            player.setShiftKeyDown(true);
            helper.useBlock(RECALL_BED_POS, player);
            player.setShiftKeyDown(false);

            helper.assertTrue(trigger.get(ModDataComponents.TRIGGER_RECALL_POS) == null,
                    "Recall position should be cleared by a shift-click unbind");
        } finally {
            MotherTriggerNetwork.get(level).unregister(absoluteBedPos);
        }

        helper.succeed();
    }

    private static void bailOutTeleportsToRecallPos(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        TriggerTestSupport.setTrion(player, 100.0f, 100.0f);

        Vec3 recallPos = helper.absoluteVec(new Vec3(1.5, 5.0, 1.5));
        trigger.set(ModDataComponents.TRIGGER_RECALL_POS, recallPos);

        TriggerTestSupport.toggleOn(player);
        TriggerTestSupport.bailOut(player);

        helper.assertTrue(player.position().distanceToSqr(recallPos) < 0.01,
                "Player should be teleported to the bound recall position on bail-out");
        helper.succeed();
    }
}
