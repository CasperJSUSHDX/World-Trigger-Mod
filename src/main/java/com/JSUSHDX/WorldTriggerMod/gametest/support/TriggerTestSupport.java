package com.JSUSHDX.WorldTriggerMod.gametest.support;

import com.JSUSHDX.WorldTriggerMod.data.ModDataAttachment;
import com.JSUSHDX.WorldTriggerMod.data.custom.TrionData;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkChannel;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;

import java.util.EnumMap;
import java.util.Map;

/**
 * Shared GameTest setup helpers for TriggerItem tests, following the project's {@code *Utils}
 * static-helper naming convention.
 */
public final class TriggerTestSupport {

    /** Fixed hotbar slot the trigger parks itself in while trigger mode is on (see TriggerModeUtils#moveTriggerToLastSlot). */
    public static final int PARKED_SLOT = 8;

    private TriggerTestSupport() {
    }

    /**
     * {@link GameTestHelper#makeMockServerPlayer} produces a player with no network connection,
     * which NPEs when {@code TriggerModeUtils.changePlayerSelectedSlot} sends its hotbar-sync
     * packet - that call happens on every toggle, on or off. {@code makeMockServerPlayerInLevel}
     * is deprecated-for-removal upstream, but it's the only helper that wires up a real
     * {@code Connection}, so it's used here to let the full production toggle path run unmodified.
     * <p>
     * That connection still never performs NeoForge's actual mod-channel handshake, so
     * {@code NetworkRegistry} would otherwise refuse to deliver {@code wtmod:set_player_slot} to it
     * ("Payload ... may not be sent to the client!" - confirmed by an actual failing test run).
     * Marking the channel negotiated directly via {@code ChannelAttributes} is the same mechanism
     * the real handshake uses to record its result, so this reproduces "the client understands this
     * payload" without needing to drive a fake client through the whole handshake.
     */
    @SuppressWarnings({"deprecation", "removal"})
    public static ServerPlayer createPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 pos = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.snapTo(pos.x, pos.y, pos.z, 0.0f, 0.0f);

        Identifier setPlayerSlotId = CommonPayload.SetPlayerSlot.TYPE.id();
        Map<ConnectionProtocol, Map<Identifier, NetworkChannel>> channels = new EnumMap<>(ConnectionProtocol.class);
        channels.put(ConnectionProtocol.PLAY, Map.of(setPlayerSlotId, new NetworkChannel(setPlayerSlotId, "1.0")));
        ChannelAttributes.setPayloadSetup(player.connection.getConnection(), new NetworkPayloadSetup(channels));

        return player;
    }

    /** Gives the player a fresh Trigger item stack, selected in hotbar slot 0. */
    public static ItemStack equipTrigger(ServerPlayer player) {
        return equip(player, ModItems.TRIGGER.get());
    }

    /** Gives the player a fresh stack of the given item, selected in hotbar slot 0. */
    public static ItemStack equip(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        player.getInventory().setItem(0, stack);
        player.getInventory().setSelectedSlot(0);
        return stack;
    }

    public static void setTrion(ServerPlayer player, float trion, float maxTrion) {
        player.setData(ModDataAttachment.TRION_DATA, new TrionData(trion, maxTrion));
    }

    /** Right-clicks whatever's in the player's currently selected slot - use while the trigger is still there. */
    public static void toggleOn(ServerPlayer player) {
        useHeldItem(player);
    }

    /**
     * Simulates scrolling to the parked trigger (hotbar slot 9) and right-clicking it (no shift) to
     * turn it off normally - the trigger lives in {@link #PARKED_SLOT} while trigger mode is on, not
     * the player's pre-toggle selected slot.
     */
    public static void toggleOff(ServerPlayer player) {
        useParkedTrigger(player, false);
    }

    /** Same as {@link #toggleOff}, but shift-clicked - the trigger's bail-out path. */
    public static void bailOut(ServerPlayer player) {
        useParkedTrigger(player, true);
    }

    private static void useParkedTrigger(ServerPlayer player, boolean shiftKeyDown) {
        player.getInventory().setSelectedSlot(PARKED_SLOT);
        player.setShiftKeyDown(shiftKeyDown);
        useHeldItem(player);
        player.setShiftKeyDown(false);
    }

    /** Right-clicks whatever's currently in the player's selected hotbar slot. */
    public static void useHeldItem(ServerPlayer player) {
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        held.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
    }
}
