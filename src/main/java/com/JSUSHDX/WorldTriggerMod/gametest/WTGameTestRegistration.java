package com.JSUSHDX.WorldTriggerMod.gametest;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.gametest.support.WTFunctionTestInstance;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

/**
 * Wires this mod's test bodies ({@link WTGameTestFunctions}, {@link WTRecallBedGameTestFunctions},
 * {@link WTSiblingTriggerGameTestFunctions}) into actual runnable tests: which structure to load,
 * which environment to use, and timing. Fired on the mod bus by NeoForge only when
 * {@code GameTestHooks.isGametestEnabled()} - i.e. exactly the situation {@code runGameTestServer}
 * already produces via {@code neoforge.enabledGameTestNamespaces}.
 */
@EventBusSubscriber(modid = WorldTriggerMod.MODID)
public class WTGameTestRegistration {

    private static final Identifier PLATFORM_STRUCTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "gametest/trigger_item_platform");
    private static final int MAX_TICKS = 20;
    private static final int SETUP_TICKS = 0;

    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment =
                event.registerEnvironment(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "default"));

        registerTest(event, environment, "toggle_on_fails_when_trion_low", WTGameTestFunctions.TOGGLE_ON_FAILS_WHEN_TRION_LOW);
        registerTest(event, environment, "toggle_on_saves_inventory_and_slot", WTGameTestFunctions.TOGGLE_ON_SAVES_INVENTORY_AND_SLOT);
        registerTest(event, environment, "toggle_on_saves_and_maxes_health", WTGameTestFunctions.TOGGLE_ON_SAVES_AND_MAXES_HEALTH);
        registerTest(event, environment, "toggle_on_toggles_trigger_state", WTGameTestFunctions.TOGGLE_ON_TOGGLES_TRIGGER_STATE);
        registerTest(event, environment, "toggle_off_restores_state", WTGameTestFunctions.TOGGLE_OFF_RESTORES_STATE);

        registerTest(event, environment, "recall_bed_bind_near_mother_trigger_sets_recall_pos",
                WTRecallBedGameTestFunctions.BIND_NEAR_MOTHER_TRIGGER_SETS_RECALL_POS);
        registerTest(event, environment, "recall_bed_bind_far_from_mother_trigger_does_not_bind",
                WTRecallBedGameTestFunctions.BIND_FAR_FROM_MOTHER_TRIGGER_DOES_NOT_BIND);
        registerTest(event, environment, "recall_bed_unbind_shift_click_clears_recall_pos",
                WTRecallBedGameTestFunctions.UNBIND_SHIFT_CLICK_CLEARS_RECALL_POS);
        registerTest(event, environment, "recall_bed_bail_out_teleports_to_recall_pos",
                WTRecallBedGameTestFunctions.BAIL_OUT_TELEPORTS_TO_RECALL_POS);

        registerTest(event, environment, "shield_trigger_on_spawns_entity", WTSiblingTriggerGameTestFunctions.SHIELD_ON_SPAWNS_ENTITY);
        registerTest(event, environment, "shield_trigger_off_discards_entity", WTSiblingTriggerGameTestFunctions.SHIELD_OFF_DISCARDS_ENTITY);
        registerTest(event, environment, "kogetsu_trigger_on_adds_attack_modifiers", WTSiblingTriggerGameTestFunctions.KOGETSU_ON_ADDS_ATTACK_MODIFIERS);
        registerTest(event, environment, "kogetsu_trigger_off_removes_attack_modifiers", WTSiblingTriggerGameTestFunctions.KOGETSU_OFF_REMOVES_ATTACK_MODIFIERS);
        // Fires bullets on a 4-tick interval via onPlayerTick - needs more headroom than the default MAX_TICKS.
        registerTest(event, environment, "asteroid_trigger_mode0_fires_bullets_over_time",
                WTSiblingTriggerGameTestFunctions.ASTEROID_MODE0_FIRES_BULLETS_OVER_TIME, 40);
        registerTest(event, environment, "asteroid_trigger_mode4_places_bullets_without_firing",
                WTSiblingTriggerGameTestFunctions.ASTEROID_MODE4_PLACES_BULLETS_WITHOUT_FIRING);
        registerTest(event, environment, "asteroid_trigger_mode4_trigger_fires_placed_bullets",
                WTSiblingTriggerGameTestFunctions.ASTEROID_MODE4_TRIGGER_FIRES_PLACED_BULLETS);
    }

    private static void registerTest(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
                                       String name, Consumer<GameTestHelper> body) {
        registerTest(event, environment, name, body, MAX_TICKS);
    }

    private static void registerTest(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
                                       String name, Consumer<GameTestHelper> body, int maxTicks) {
        Identifier id = Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, name);
        WTFunctionTestInstance instance = new WTFunctionTestInstance(body,
                new TestData<>(environment, PLATFORM_STRUCTURE, maxTicks, SETUP_TICKS, true));
        event.registerTest(id, instance);
    }
}
