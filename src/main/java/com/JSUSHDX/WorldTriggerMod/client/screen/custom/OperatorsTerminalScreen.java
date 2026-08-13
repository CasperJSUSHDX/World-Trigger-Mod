package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.OperatorsTerminalMenu;
import com.JSUSHDX.WorldTriggerMod.client.screen.base.BaseMachineScreen;
import com.JSUSHDX.WorldTriggerMod.data.records.TerminalEntry;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class OperatorsTerminalScreen extends BaseMachineScreen<OperatorsTerminalMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/gui/operators_terminal_gui.png");

    private static final int LIST_LEFT = 8;
    private static final int LIST_RIGHT = 168;
    private static final int LIST_TOP = 26;
    private static final int ROW_HEIGHT = 10;
    private static final int ROWS_VISIBLE = 8;
    private static final int REMOVE_BUTTON_WIDTH = 8;
    private static final int REMOVE_BUTTON_LEFT = LIST_RIGHT - REMOVE_BUTTON_WIDTH;

    private int scrollOffset = 0;

    public OperatorsTerminalScreen(OperatorsTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 176, 210);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 118;
    }

    private List<TerminalEntry> entries() {
        return this.menu.getBlockEntity().getEntries();
    }

    private int maxScrollOffset() {
        return Math.max(0, entries().size() - ROWS_VISIBLE);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        super.extractLabels(guiGraphicsExtractor, mouseX, mouseY);

        List<TerminalEntry> entries = entries();
        if (entries.isEmpty()) {
            Component empty = Component.translatable("message.wtmod.terminal_no_entries");
            int textWidth = this.font.width(empty);
            guiGraphicsExtractor.text(this.font, empty, (176 - textWidth) / 2, LIST_TOP + 4, 0xFF808080, false);
            return;
        }

        int offset = Math.min(this.scrollOffset, maxScrollOffset());
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int index = offset + i;
            if (index >= entries.size()) {
                break;
            }

            TerminalEntry entry = entries.get(index);
            int rowY = LIST_TOP + i * ROW_HEIGHT;

            if (mouseX >= LIST_LEFT && mouseX < LIST_RIGHT && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                guiGraphicsExtractor.fill(LIST_LEFT, rowY, LIST_RIGHT, rowY + ROW_HEIGHT, 0x30FFFFFF);
            }

            guiGraphicsExtractor.text(this.font, entry.ownerName(), LIST_LEFT + 2, rowY + 1, 0xFF404040, false);

            String trionText = (int) entry.trion() + " / " + (int) entry.maxTrion();
            int trionColor = entry.maxTrion() > 0 && entry.trion() / entry.maxTrion() < 0.34f ? 0xFFB71C1C : 0xFF2E7D32;
            int trionWidth = this.font.width(trionText);
            guiGraphicsExtractor.text(this.font, trionText, REMOVE_BUTTON_LEFT - 3 - trionWidth, rowY + 1, trionColor, false);

            guiGraphicsExtractor.fill(REMOVE_BUTTON_LEFT, rowY, LIST_RIGHT, rowY + ROW_HEIGHT, 0x40FF0000);
            guiGraphicsExtractor.text(this.font, "x", REMOVE_BUTTON_LEFT + 1, rowY + 1, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int localX = (int) event.x() - this.leftPos;
        int localY = (int) event.y() - this.topPos;

        List<TerminalEntry> entries = entries();
        if (event.button() == 0 && !entries.isEmpty()) {
            int offset = Math.min(this.scrollOffset, maxScrollOffset());
            for (int i = 0; i < ROWS_VISIBLE; i++) {
                int index = offset + i;
                if (index >= entries.size()) {
                    break;
                }

                int rowY = LIST_TOP + i * ROW_HEIGHT;
                if (localX >= REMOVE_BUTTON_LEFT && localX < LIST_RIGHT && localY >= rowY && localY < rowY + ROW_HEIGHT) {
                    BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                    ClientPacketDistributor.sendToServer(new CommonPayload.RemoveTerminalEntry(pos, entries.get(index).ownerId()));
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
