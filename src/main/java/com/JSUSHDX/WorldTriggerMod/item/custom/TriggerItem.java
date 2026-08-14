package com.JSUSHDX.WorldTriggerMod.item.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.OperatorsTerminalBlock;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.RecallBedBlock;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.OperatorsTerminalBlockEntity;
import com.JSUSHDX.WorldTriggerMod.data.ModDataAttachment;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.data.custom.MotherTriggerNetwork;
import com.JSUSHDX.WorldTriggerMod.combat.CombatSimulateManager;
import com.JSUSHDX.WorldTriggerMod.data.custom.TrionData;
import com.JSUSHDX.WorldTriggerMod.data.records.HealthData;
import com.JSUSHDX.WorldTriggerMod.data.records.InventoryData;
import com.JSUSHDX.WorldTriggerMod.data.records.TriggerConfigureData;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import com.JSUSHDX.WorldTriggerMod.util.TriggerStateUtils;
import com.JSUSHDX.WorldTriggerMod.util.TrionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

public class TriggerItem extends Item {
    public TriggerItem(Properties properties) {
        super(properties);
    }

    private static void savePlayerInventoryToTrigger(Player player, ItemStack itemStack) {
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

    private static void restoreTriggerSavedInventoryToPlayer(Player player, ItemStack itemStack) {
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

    private static void rewindPlayerHealth(Player player, ItemStack itemStack) {
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

    private static void ProvideChosenTriggers(Player player, ItemStack itemStack) {
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

    private static void changePlayerSelectedSlot(Player player, int slot) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new CommonPayload.SetPlayerSlot(slot));
        }
    }

    private static void moveTriggerToLastSlot(Player player, ItemStack itemStack) {
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

    private static void recoverTriggerBeforeSlot (Player player, ItemStack itemStack) {
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

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block block = level.getBlockState(pos).getBlock();

        if (block instanceof RecallBedBlock) {
            return bindOrUnbindRecallBed(level, pos, player, context.getItemInHand());
        }

        if (block instanceof OperatorsTerminalBlock) {
            return bindOrUnbindOperatorsTerminal(level, pos, player);
        }

        return super.useOn(context);
    }

    private static InteractionResult bindOrUnbindRecallBed(Level level, BlockPos pos, Player player, ItemStack itemStack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!MotherTriggerNetwork.isNearMotherTrigger(level, pos)) {
            player.sendSystemMessage(Component.translatable("message.wtmod.out_of_mother_trigger_range"));
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            itemStack.remove(ModDataComponents.TRIGGER_RECALL_POS);
            player.sendSystemMessage(Component.translatable("message.wtmod.unbind_recall_pos"));
            return InteractionResult.SUCCESS;
        }

        Vec3 position = Vec3.atCenterOf(pos).add(0, 1, 0);
        itemStack.set(ModDataComponents.TRIGGER_RECALL_POS, position);

        Component msg = Component.translatable("message.wtmod.bind_recall_bed", position.x, position.y, position.z);
        player.sendSystemMessage(msg);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult bindOrUnbindOperatorsTerminal(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!MotherTriggerNetwork.isNearMotherTrigger(level, pos)) {
            player.sendSystemMessage(Component.translatable("message.wtmod.out_of_mother_trigger_range"));
            return InteractionResult.FAIL;
        }

        if (!(level.getBlockEntity(pos) instanceof OperatorsTerminalBlockEntity blockEntity)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            blockEntity.removeEntry(player.getUUID());
            player.sendSystemMessage(Component.translatable("message.wtmod.terminal_unregistered", player.getName()));
            return InteractionResult.SUCCESS;
        }

        blockEntity.registerEntry(player);
        player.sendSystemMessage(Component.translatable("message.wtmod.terminal_registered", player.getName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
            TrionData data = player.getData(ModDataAttachment.TRION_DATA);

            if (!isOn && data.trion() < data.maxTrion() * 0.8) {
                // Not enough Trion to transform
                player.sendSystemMessage(Component.translatable("message.wtmod.trion_not_enough"));
                return InteractionResult.SUCCESS;
            }

            if (!isOn) {
                // Inventory operations
                savePlayerInventoryToTrigger(player, itemStack);
                moveTriggerToLastSlot(player, itemStack);
                changePlayerSelectedSlot(player, 0);

                // Data components operations
                itemStack.set(ModDataComponents.IS_ON, true);
                itemStack.set(ModDataComponents.HEALTH_DATA, new HealthData(player.getHealth(), player.getMaxHealth()));

                TriggerStateUtils.toggleState(player);

                player.setHealth(player.getMaxHealth());

                ProvideChosenTriggers(player, itemStack);
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    if (player.isShiftKeyDown()) {
                        bailOut(serverPlayer, itemStack);
                    } else {
                        resetPlayerStatus(serverPlayer,  itemStack);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static void resetPlayerStatus(ServerPlayer player, ItemStack itemStack) {
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

    public static void bailOut(ServerPlayer player, ItemStack itemStack) {
        resetPlayerStatus(player, itemStack);

        // If this player is inside a running combat simulation, that has its own return-teleport
        // (back to wherever they were before it started) which takes priority over the recall bed -
        // the recall bed is for real away-mission use, not for leaving a training arena.
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

    @Override
    // Give enchanted visual effect
    public boolean isFoil(ItemStack itemStack) {
        Boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
        return isOn;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        Boolean isOn = itemStack.getOrDefault(ModDataComponents.IS_ON, false);
        Component msg = Component.translatable("tooltip.wtmod.is_on", (isOn ? "On" : "Off")).withColor(TextColor.GREEN);
        builder.accept(msg);
    }
}
