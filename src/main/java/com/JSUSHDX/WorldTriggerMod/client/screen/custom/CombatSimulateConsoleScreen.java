package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.CombatSimulateConsoleMenu;
import com.JSUSHDX.WorldTriggerMod.client.screen.base.BaseMachineScreen;
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

    private static final int LIST_LEFT = 8;
    private static final int LIST_RIGHT = 168;
    private static final int LIST_TOP = 20;
    private static final int ROW_HEIGHT = 10;
    private static final int ROWS_VISIBLE = 7;
    private static final int REMOVE_BUTTON_WIDTH = 8;
    private static final int REMOVE_BUTTON_LEFT = LIST_RIGHT - REMOVE_BUTTON_WIDTH;

    private static final int BUTTON_LEFT = 38;
    private static final int BUTTON_TOP = 97;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 14;

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
                    guiGraphicsExtractor.fill(LIST_LEFT, rowY, LIST_RIGHT, rowY + ROW_HEIGHT, 0x30FFFFFF);
                }

                Component rowText = Component.literal("#" + (index + 1) + " ").append(environment.describe());
                guiGraphicsExtractor.text(this.font, rowText, LIST_LEFT + 2, rowY + 1, 0xFF404040, false);

                guiGraphicsExtractor.fill(REMOVE_BUTTON_LEFT, rowY, LIST_RIGHT, rowY + ROW_HEIGHT, 0x40FF0000);
                guiGraphicsExtractor.text(this.font, "x", REMOVE_BUTTON_LEFT + 1, rowY + 1, 0xFFFFFFFF, false);
            }
        }

        boolean buttonHovered = localMouseX >= BUTTON_LEFT && localMouseX < BUTTON_LEFT + BUTTON_WIDTH
                && localMouseY >= BUTTON_TOP && localMouseY < BUTTON_TOP + BUTTON_HEIGHT;
        guiGraphicsExtractor.fill(BUTTON_LEFT, BUTTON_TOP, BUTTON_LEFT + BUTTON_WIDTH, BUTTON_TOP + BUTTON_HEIGHT,
                buttonHovered ? 0xFF5A5A5A : 0xFF3A3A3A);
        Component buttonLabel = Component.translatable("gui.wtmod.combat_simulate_console.generate");
        int labelWidth = this.font.width(buttonLabel);
        guiGraphicsExtractor.text(this.font, buttonLabel,
                BUTTON_LEFT + (BUTTON_WIDTH - labelWidth) / 2, BUTTON_TOP + (BUTTON_HEIGHT - 8) / 2,
                0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int localX = (int) event.x() - this.leftPos;
        int localY = (int) event.y() - this.topPos;

        if (event.button() == 0) {
            if (localX >= BUTTON_LEFT && localX < BUTTON_LEFT + BUTTON_WIDTH
                    && localY >= BUTTON_TOP && localY < BUTTON_TOP + BUTTON_HEIGHT) {
                BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                ClientPacketDistributor.sendToServer(new CommonPayload.AddCombatPoolEntry(pos));
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
                    if (localX >= REMOVE_BUTTON_LEFT && localX < LIST_RIGHT && localY >= rowY && localY < rowY + ROW_HEIGHT) {
                        BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                        ClientPacketDistributor.sendToServer(new CommonPayload.RemoveCombatPoolEntry(pos, index));
                        return true;
                    }
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
