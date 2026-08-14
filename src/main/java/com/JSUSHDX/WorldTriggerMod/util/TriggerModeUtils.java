package com.JSUSHDX.WorldTriggerMod.util;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.combat.CombatSimulateManager;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.records.HealthData;
import com.JSUSHDX.WorldTriggerMod.data.records.InventoryData;
import com.JSUSHDX.WorldTriggerMod.data.records.TriggerConfigureData;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.phys.Vec3;

/**
 * Player-state bookkeeping for entering/leaving trigger mode: saving and restoring the player's
 * inventory and health, moving the trigger item to/from its dedicated hotbar slot, and (via
 * {@link #bailOut}) the full "turn trigger off" lifecycle including deferring to
 * {@link CombatSimulateManager} when the player is bailing out of a running combat simulation.
 * Extracted from {@code TriggerItem} so the item class only has to orchestrate calls into here
 * rather than implement the bookkeeping itself - none of these methods touch the item instance,
 * they only operate on {@link Player}/{@link ItemStack} data.
 */
public class TriggerModeUtils {
    private TriggerModeUtils() {
    }

    public static void savePlayerInventoryToTrigger(Player player, ItemStack itemStack) {
        int invSize = player.getInventory().getContainerSize();
        NonNullList<ItemStack> invItems = NonNullList.withSize(invSize, ItemStack.EMPTY);

        for (int i = 0; i < invSize; ++i) {
            ItemStack currentItemStack = player.getInventory().getItem(i);

            if (!currentItemStack.isEmpty() && currentItemStack != itemStack) {
                invItems.set(i, currentItemStack.copy());
            }
        }

        // Package to components
        ItemContainerContents containerContents = ItemContainerContents.fromItems(invItems);

        // Write into trigger
        itemStack.set(ModDataComponents.INVENTORY_DATA, new InventoryData(containerContents));

        // Clean player inventory (Keep trigger)
        for (int i = 0; i < invSize; ++i) {
            if (player.getInventory().getItem(i) != itemStack) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static void restoreTriggerSavedInventoryToPlayer(Player player, ItemStack itemStack) {
        InventoryData data = itemStack.get(ModDataComponents.INVENTORY_DATA);
        ItemContainerContents containerContents = (data != null) ? data.contents() : null;

        // End if there is no data
        if (containerContents == null) return;

        int invSize = player.getInventory().getContainerSize();
        NonNullList<ItemStack> restoredItems = NonNullList.withSize(invSize, ItemStack.EMPTY);
        containerContents.copyInto(restoredItems);

        // Pub back to player inventory
        for (int i = 0; i < invSize; i++) {
            if (i < restoredItems.size()) {
                if (player.getInventory().getItem(i) != itemStack) {
                    player.getInventory().setItem(i, restoredItems.get(i));
                }
            }
        }

        // Clean the data component on trigger
        itemStack.remove(ModDataComponents.INVENTORY_DATA);
    }

    public static void rewindPlayerHealth(Player player, ItemStack itemStack) {
        HealthData data = itemStack.getOrDefault(ModDataComponents.HEALTH_DATA, new HealthData(20.0f, 20.0f));
        // Set max health
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        Identifier modifierID = Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "trigger_rewind_max_health");
        if (maxHealthAttr != null) {
            maxHealthAttr.removeModifier(modifierID);
            double currentMaxHealth = maxHealthAttr.getValue();
            double delta = data.max() - currentMaxHealth;
            if (delta > 0) {
                AttributeModifier modifier = new AttributeModifier(
                        modifierID,
                        delta,
                        AttributeModifier.Operation.ADD_VALUE
                );
                maxHealthAttr.addPermanentModifier(modifier);
            }
        }

        // Heal to record health
        player.setHealth(data.current());
        itemStack.remove(ModDataComponents.HEALTH_DATA);
    }

    public static void provideChosenTriggers(Player player, ItemStack itemStack) {
        TriggerConfigureData config = itemStack.getOrDefault(ModDataComponents.TRIGGER_CONFIGURE,
                new TriggerConfigureData());

        int slot = 0;
        for (Item trigger : config.triggers()) {
            if (trigger != null) {
                player.getInventory().setItem(slot, new ItemStack(trigger));
            }

            slot++;
        }
    }

    public static void changePlayerSelectedSlot(Player player, int slot) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new CommonPayload.SetPlayerSlot(slot));
        }
    }

    public static void moveTriggerToLastSlot(Player player, ItemStack itemStack) {
        // Move trigger to the 9th slot (index 8)
        int triggerSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == itemStack) {
                triggerSlot = i;
                break;
            }
        }

        itemStack.set(ModDataComponents.USED_SLOT, triggerSlot);
        if (triggerSlot != -1 && triggerSlot != 8) {
            player.getInventory().setItem(8, itemStack);
            player.getInventory().setItem(triggerSlot, ItemStack.EMPTY);
        }
    }

    public static void recoverTriggerBeforeSlot(Player player, ItemStack itemStack) {
        int usedSlot = itemStack.getOrDefault(ModDataComponents.USED_SLOT, 0);

        int triggerSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == itemStack) {
                triggerSlot = i;
                break;
            }
        }

        if (triggerSlot != -1 && usedSlot != -1 && triggerSlot != usedSlot) {
            player.getInventory().setItem(usedSlot, itemStack);
            player.getInventory().setItem(triggerSlot, ItemStack.EMPTY);
        }

        changePlayerSelectedSlot(player, usedSlot);
        itemStack.remove(ModDataComponents.USED_SLOT);
    }

    /**
     * Turns trigger mode off: restores the player's saved inventory/health/slot and toggles the
     * morph state back. Used both for a normal "turn off" and as the first step of {@link #bailOut}.
     */
    public static void resetPlayerStatus(ServerPlayer player, ItemStack itemStack) {
        // Inventory operations
        recoverTriggerBeforeSlot(player, itemStack);
        restoreTriggerSavedInventoryToPlayer(player, itemStack);

        // Data components operations
        itemStack.set(ModDataComponents.IS_ON, false);
        rewindPlayerHealth(player, itemStack);

        // Clear effects
        player.removeAllEffects();
        player.clearFire();
        player.clearFreeze();

        TriggerStateUtils.toggleState(player);
    }

    /**
     * Forces a player's trigger off, e.g. from a shift-click or from trion hitting zero. After
     * resetting their status, defers to {@link CombatSimulateManager#onPlayerBailout} first - if
     * the player is inside a running combat simulation, its own return-teleport takes priority
     * over the recall bed (the recall bed is for real away-mission use, not for leaving a
     * training arena).
     */
    public static void bailOut(ServerPlayer player, ItemStack itemStack) {
        resetPlayerStatus(player, itemStack);

        boolean trionDepleted = TrionUtils.getTrion(player) <= 0;
        if (CombatSimulateManager.onPlayerBailout(player, trionDepleted)) {
            return;
        }

        // Teleport to recall bed
        Vec3 position = itemStack.get(ModDataComponents.TRIGGER_RECALL_POS);
        if (position != null) {
            player.teleportTo(position.x, position.y, position.z);
        }
    }
}
