package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.CombatSimulateConsoleMenu;
import com.JSUSHDX.WorldTriggerMod.client.screen.base.BaseMachineScreen;
import com.JSUSHDX.WorldTriggerMod.combat.CombatSimulationSummary;
import com.JSUSHDX.WorldTriggerMod.data.records.CombatEnvironment;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class CombatSimulateConsoleScreen extends BaseMachineScreen<CombatSimulateConsoleMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/gui/combat_simulate_console_gui.png");

    // One row's worth of height (10px) was reclaimed from the pool list for the ongoing-simulation
    // status line below, so the whole panel still fits inside the existing 176x210 texture.
    private static final int STATUS_LINE_Y = 20;
    private static final int LIST_LEFT = 8;
    private static final int LIST_RIGHT = 168;
    private static final int LIST_TOP = 30;
    private static final int ROW_HEIGHT = 10;
    private static final int ROWS_VISIBLE = 6;
    private static final int REMOVE_BUTTON_WIDTH = 8;
    private static final int REMOVE_BUTTON_LEFT = LIST_RIGHT - REMOVE_BUTTON_WIDTH;
    private static final int GEAR_BUTTON_WIDTH = 8;
    private static final int GEAR_BUTTON_GAP = 1;
    private static final int GEAR_BUTTON_LEFT = REMOVE_BUTTON_LEFT - GEAR_BUTTON_GAP - GEAR_BUTTON_WIDTH;

    private static final int BUTTON_LEFT = 38;
    private static final int BUTTON_TOP = 97;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 14;

    // Shared with CombatEnvironmentPickerScreen - matches the light gray/white theme sampled
    // from combat_simulate_console_gui.png (panel 0xC6C6C6, border 0x555555) so the hand-drawn
    // UI elements blend into the console's background instead of the earlier dark-theme mockup.
    private static final int COLOR_BUTTON_IDLE = 0xFFB0B0B0;
    private static final int COLOR_BUTTON_HOVER = 0xFFCFCFCF;
    private static final int COLOR_BUTTON_BORDER = 0xFF555555;
    private static final int COLOR_BUTTON_TEXT = 0xFF202020;
    private static final int COLOR_ROW_HOVER = 0x30000000;
    private static final int COLOR_ROW_TEXT = 0xFF404040;
    private static final int COLOR_REMOVE_BUTTON = 0xFFB33A3A;
    private static final int COLOR_REMOVE_TEXT = 0xFFFFFFFF;
    private static final int COLOR_GEAR_BUTTON = 0xFFB0B0B0;
    private static final int COLOR_GEAR_BUTTON_HOVER = 0xFFCFCFCF;
    private static final int COLOR_GEAR_TEXT = 0xFF202020;
    private static final int COLOR_STATUS_TEXT = 0xFF2E6B2E;
    private static final int COLOR_STATUS_TEXT_EMPTY = 0xFF808080;

    private int scrollOffset = 0;

    public CombatSimulateConsoleScreen(CombatSimulateConsoleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 176, 210);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 118;
    }

    private List<CombatEnvironment> combatPool() {
        return this.menu.getBlockEntity().getCombatPool();
    }

    private int maxScrollOffset() {
        return Math.max(0, combatPool().size() - ROWS_VISIBLE);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        super.extractLabels(guiGraphicsExtractor, mouseX, mouseY);

        // extractLabels/renderLabels receives mouse coordinates in absolute screen space, but
        // drawing here happens inside a pose already translated to (leftPos, topPos) - convert
        // to the same local space before doing any hover/hit-testing.
        int localMouseX = mouseX - this.leftPos;
        int localMouseY = mouseY - this.topPos;

        drawStatusLine(guiGraphicsExtractor);

        List<CombatEnvironment> combatPool = combatPool();
        if (combatPool.isEmpty()) {
            Component empty = Component.translatable("message.wtmod.combat_pool_empty");
            int textWidth = this.font.width(empty);
            guiGraphicsExtractor.text(this.font, empty, (176 - textWidth) / 2, LIST_TOP + 4, 0xFF808080, false);
        } else {
            int offset = Math.min(this.scrollOffset, maxScrollOffset());
            for (int i = 0; i < ROWS_VISIBLE; i++) {
                int index = offset + i;
                if (index >= combatPool.size()) {
                    break;
                }

                CombatEnvironment environment = combatPool.get(index);
                int rowY = LIST_TOP + i * ROW_HEIGHT;

                if (localMouseX >= LIST_LEFT && localMouseX < LIST_RIGHT && localMouseY >= rowY && localMouseY < rowY + ROW_HEIGHT) {
                    guiGraphicsExtractor.fill(LIST_LEFT, rowY, LIST_RIGHT, rowY + ROW_HEIGHT, COLOR_ROW_HOVER);
                }

                Component rowText = Component.literal("#" + (index + 1) + " ").append(environment.describe());
                guiGraphicsExtractor.text(this.font, rowText, LIST_LEFT + 2, rowY + 1, COLOR_ROW_TEXT, false);

                boolean gearHovered = localMouseX >= GEAR_BUTTON_LEFT && localMouseX < GEAR_BUTTON_LEFT + GEAR_BUTTON_WIDTH
                        && localMouseY >= rowY && localMouseY < rowY + ROW_HEIGHT;
                guiGraphicsExtractor.fill(GEAR_BUTTON_LEFT, rowY, GEAR_BUTTON_LEFT + GEAR_BUTTON_WIDTH, rowY + ROW_HEIGHT,
                        gearHovered ? COLOR_GEAR_BUTTON_HOVER : COLOR_GEAR_BUTTON);
                guiGraphicsExtractor.text(this.font, "⚙", GEAR_BUTTON_LEFT + 1, rowY + 1, COLOR_GEAR_TEXT, false);

                guiGraphicsExtractor.fill(REMOVE_BUTTON_LEFT, rowY, LIST_RIGHT, rowY + ROW_HEIGHT, COLOR_REMOVE_BUTTON);
                guiGraphicsExtractor.text(this.font, "x", REMOVE_BUTTON_LEFT + 1, rowY + 1, COLOR_REMOVE_TEXT, false);
            }
        }

        boolean buttonHovered = localMouseX >= BUTTON_LEFT && localMouseX < BUTTON_LEFT + BUTTON_WIDTH
                && localMouseY >= BUTTON_TOP && localMouseY < BUTTON_TOP + BUTTON_HEIGHT;
        guiGraphicsExtractor.fill(BUTTON_LEFT - 1, BUTTON_TOP - 1, BUTTON_LEFT + BUTTON_WIDTH + 1, BUTTON_TOP + BUTTON_HEIGHT + 1, COLOR_BUTTON_BORDER);
        guiGraphicsExtractor.fill(BUTTON_LEFT, BUTTON_TOP, BUTTON_LEFT + BUTTON_WIDTH, BUTTON_TOP + BUTTON_HEIGHT,
                buttonHovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_IDLE);
        Component buttonLabel = Component.translatable("gui.wtmod.combat_simulate_console.generate");
        int labelWidth = this.font.width(buttonLabel);
        guiGraphicsExtractor.text(this.font, buttonLabel,
                BUTTON_LEFT + (BUTTON_WIDTH - labelWidth) / 2, BUTTON_TOP + (BUTTON_HEIGHT - 8) / 2,
                COLOR_BUTTON_TEXT, false);
    }

    /**
     * Compact one-line "ongoing simulations" readout - there isn't enough spare vertical space
     * in the existing 176x210 texture for a full second list panel, so this shows a count plus
     * the soonest-ending simulation's countdown instead. Click a pool row (not its remove "x")
     * below to start a new one.
     */
    private void drawStatusLine(GuiGraphicsExtractor guiGraphicsExtractor) {
        List<CombatSimulationSummary> active = this.menu.getBlockEntity().getActiveSimulations();

        Component text;
        int color;
        if (active.isEmpty()) {
            text = Component.translatable("gui.wtmod.combat_simulate_console.active_none");
            color = COLOR_STATUS_TEXT_EMPTY;
        } else {
            CombatSimulationSummary soonest = active.stream()
                    .min((a, b) -> Integer.compare(a.remainingSeconds(), b.remainingSeconds()))
                    .orElse(active.get(0));
            text = Component.translatable("gui.wtmod.combat_simulate_console.active_count",
                    active.size(), soonest.participantNames(), soonest.remainingSeconds());
            color = COLOR_STATUS_TEXT;
        }

        int textWidth = this.font.width(text);
        guiGraphicsExtractor.text(this.font, text, Math.max(LIST_LEFT, (176 - textWidth) / 2), STATUS_LINE_Y, color, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int localX = (int) event.x() - this.leftPos;
        int localY = (int) event.y() - this.topPos;

        if (event.button() == 0) {
            if (localX >= BUTTON_LEFT && localX < BUTTON_LEFT + BUTTON_WIDTH
                    && localY >= BUTTON_TOP && localY < BUTTON_TOP + BUTTON_HEIGHT) {
                BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                this.minecraft.setScreenAndShow(new CombatEnvironmentPickerScreen(this, pos));
                return true;
            }

            List<CombatEnvironment> combatPool = combatPool();
            if (!combatPool.isEmpty()) {
                int offset = Math.min(this.scrollOffset, maxScrollOffset());
                for (int i = 0; i < ROWS_VISIBLE; i++) {
                    int index = offset + i;
                    if (index >= combatPool.size()) {
                        break;
                    }

                    int rowY = LIST_TOP + i * ROW_HEIGHT;
                    if (localY < rowY || localY >= rowY + ROW_HEIGHT || localX < LIST_LEFT || localX >= LIST_RIGHT) {
                        continue;
                    }

                    BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                    if (localX >= REMOVE_BUTTON_LEFT) {
                        ClientPacketDistributor.sendToServer(new CommonPayload.RemoveCombatPoolEntry(pos, index));
                    } else if (localX >= GEAR_BUTTON_LEFT) {
                        this.minecraft.setScreenAndShow(new CombatEnvironmentPickerScreen(this, pos, index, combatPool.get(index)));
                    } else {
                        // Clicking anywhere else on the row starts a simulation with that scenario -
                        // the server removes it from the pool immediately on a successful start, so
                        // a running instance simply isn't in this list anymore to click again.
                        ClientPacketDistributor.sendToServer(new CommonPayload.StartCombatSimulation(pos, index));
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int localX = (int) x - this.leftPos;
        int localY = (int) y - this.topPos;

        if (localX >= LIST_LEFT && localX < LIST_RIGHT && localY >= LIST_TOP && localY < LIST_TOP + ROWS_VISIBLE * ROW_HEIGHT) {
            this.scrollOffset = Math.max(0, Math.min(maxScrollOffset(), this.scrollOffset - (int) Math.signum(scrollY)));
            return true;
        }

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }
}
