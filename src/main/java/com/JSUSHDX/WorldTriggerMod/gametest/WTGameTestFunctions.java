package com.JSUSHDX.WorldTriggerMod.gametest;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.records.HealthData;
import com.JSUSHDX.WorldTriggerMod.data.records.InventoryData;
import com.JSUSHDX.WorldTriggerMod.gametest.support.TriggerTestSupport;
import com.JSUSHDX.WorldTriggerMod.util.TriggerStateUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * This mod's GameTest bodies. MC 26.2's GameTest framework has no {@code @GameTest} annotation -
 * these are plain functions, wired up to a structure/environment/schedule by
 * {@link WTGameTestRegistration} handling NeoForge's {@code RegisterGameTestsEvent}. They're
 * exposed as method references rather than registered into {@code Registries.TEST_FUNCTION}
 * (see {@link com.JSUSHDX.WorldTriggerMod.gametest.support.WTFunctionTestInstance} for why that
 * registry doesn't work for mod-added test bodies in this NeoForge version).
 */
public class WTGameTestFunctions {

    public static final Consumer<GameTestHelper> TOGGLE_ON_FAILS_WHEN_TRION_LOW = WTGameTestFunctions::toggleOnFailsWhenTrionLow;
    public static final Consumer<GameTestHelper> TOGGLE_ON_SAVES_INVENTORY_AND_SLOT = WTGameTestFunctions::toggleOnSavesInventoryAndSlot;
    public static final Consumer<GameTestHelper> TOGGLE_ON_SAVES_AND_MAXES_HEALTH = WTGameTestFunctions::toggleOnSavesAndMaxesHealth;
    public static final Consumer<GameTestHelper> TOGGLE_ON_TOGGLES_TRIGGER_STATE = WTGameTestFunctions::toggleOnTogglesTriggerState;
    public static final Consumer<GameTestHelper> TOGGLE_OFF_RESTORES_STATE = WTGameTestFunctions::toggleOffRestoresState;

    private WTGameTestFunctions() {
    }

    private static void toggleOnFailsWhenTrionLow(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        TriggerTestSupport.setTrion(player, 50.0f, 100.0f); // below the 80% activation threshold

        TriggerTestSupport.toggleOn(player);

        helper.assertFalse(trigger.getOrDefault(ModDataComponents.IS_ON, false), "Trigger should not turn on below the trion threshold");
        helper.assertTrue(trigger.get(ModDataComponents.INVENTORY_DATA) == null, "Inventory should not be captured when the toggle is rejected");
        helper.succeed();
    }

    private static void toggleOnSavesInventoryAndSlot(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        TriggerTestSupport.setTrion(player, 100.0f, 100.0f);

        ItemStack marker = new ItemStack(Items.DIRT, 5);
        player.getInventory().setItem(1, marker);

        TriggerTestSupport.toggleOn(player);

        helper.assertTrue(trigger.getOrDefault(ModDataComponents.IS_ON, false), "Trigger should be on");
        InventoryData savedInventory = trigger.get(ModDataComponents.INVENTORY_DATA);
        helper.assertTrue(savedInventory != null, "Inventory should be captured on the trigger item");
        helper.assertTrue(player.getInventory().getItem(1).isEmpty(), "Original inventory slot should be cleared");
        helper.assertTrue(player.getInventory().getItem(TriggerTestSupport.PARKED_SLOT) == trigger,
                "Trigger should be parked in hotbar slot 9 (index 8)");
        helper.succeed();
    }

    private static void toggleOnSavesAndMaxesHealth(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        TriggerTestSupport.setTrion(player, 100.0f, 100.0f);
        player.setHealth(10.0f);

        TriggerTestSupport.toggleOn(player);

        HealthData savedHealth = trigger.get(ModDataComponents.HEALTH_DATA);
        helper.assertTrue(savedHealth != null, "Health should be captured on the trigger item");
        helper.assertValueEqual(savedHealth.current(), 10.0f, "saved current health");
        helper.assertValueEqual(player.getHealth(), player.getMaxHealth(), "player health after toggling on");
        helper.succeed();
    }

    private static void toggleOnTogglesTriggerState(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        TriggerTestSupport.equipTrigger(player);
        TriggerTestSupport.setTrion(player, 100.0f, 100.0f);

        helper.assertFalse(TriggerStateUtils.getState(player), "Trigger state should start off");
        TriggerTestSupport.toggleOn(player);
        helper.assertTrue(TriggerStateUtils.getState(player), "Trigger state should flip on after toggling");
        helper.succeed();
    }

    private static void toggleOffRestoresState(GameTestHelper helper) {
        ServerPlayer player = TriggerTestSupport.createPlayer(helper);
        ItemStack trigger = TriggerTestSupport.equipTrigger(player);
        TriggerTestSupport.setTrion(player, 100.0f, 100.0f);

        ItemStack marker = new ItemStack(Items.DIRT, 5);
        player.getInventory().setItem(1, marker);
        float initialHealth = 14.0f;
        player.setHealth(initialHealth);

        TriggerTestSupport.toggleOn(player);
        helper.assertTrue(trigger.getOrDefault(ModDataComponents.IS_ON, false), "Precondition: trigger should be on before testing toggle-off");

        TriggerTestSupport.toggleOff(player);

        helper.assertFalse(trigger.getOrDefault(ModDataComponents.IS_ON, false), "Trigger should be off");
        helper.assertFalse(TriggerStateUtils.getState(player), "Trigger state should flip back off");
        ItemStack restoredMarker = player.getInventory().getItem(1);
        helper.assertTrue(restoredMarker.is(marker.getItem()) && restoredMarker.getCount() == marker.getCount(),
                "Inventory item should be restored to its original slot");
        helper.assertValueEqual(player.getHealth(), initialHealth, "player health after toggling off");
        helper.succeed();
    }
}
