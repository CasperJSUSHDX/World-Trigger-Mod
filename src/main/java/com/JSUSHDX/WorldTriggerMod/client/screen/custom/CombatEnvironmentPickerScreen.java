package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.data.records.CombatEnvironment;
import com.JSUSHDX.WorldTriggerMod.network.CommonPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Standalone picker that pops up over {@link CombatSimulateConsoleScreen} when the player
 * presses the console's "add scenario" button. Lets the player choose a time of day and a
 * weather before the combination is sent to the server and appended to the console's
 * combat pool. The underlying container menu stays open the whole time - this only ever
 * swaps the client-side {@code Screen}, it never closes/reopens the menu.
 */
public class CombatEnvironmentPickerScreen extends Screen {
    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_HEIGHT = 120;
    private static final int OPTION_HEIGHT = 16;
    private static final int OPTION_GAP = 4;
    private static final int ACTION_BUTTON_WIDTH = 64;

    // Light gray/white theme sampled from combat_simulate_console_gui.png (panel 0xC6C6C6,
    // border 0x555555) so this picker matches CombatSimulateConsoleScreen instead of standing
    // out as a separate dark-themed dialog.
    private static final int COLOR_SCRIM = 0x88000000;
    private static final int COLOR_PANEL_BG = 0xFFC6C6C6;
    private static final int COLOR_PANEL_BORDER = 0xFF555555;
    private static final int COLOR_TITLE_TEXT = 0xFF202020;
    private static final int COLOR_SECTION_LABEL = 0xFF606060;
    private static final int COLOR_BUTTON_IDLE = 0xFFB0B0B0;
    private static final int COLOR_BUTTON_HOVER = 0xFFCFCFCF;
    private static final int COLOR_BUTTON_TEXT = 0xFF202020;
    private static final int COLOR_SELECTED_BG = 0xFF3A6EA5;
    private static final int COLOR_SELECTED_TEXT = 0xFFFFFFFF;

    private static final CombatEnvironment.TimeOfDay[] TIMES = CombatEnvironment.TimeOfDay.values();
    private static final CombatEnvironment.Weather[] WEATHERS = CombatEnvironment.Weather.values();

    private final Screen parent;
    private final BlockPos consolePos;

    private CombatEnvironment.TimeOfDay selectedTime = CombatEnvironment.TimeOfDay.MORNING;
    private CombatEnvironment.Weather selectedWeather = CombatEnvironment.Weather.SUNNY;

    public CombatEnvironmentPickerScreen(Screen parent, BlockPos consolePos) {
        super(Component.translatable("gui.wtmod.combat_environment_picker.title"));
        this.parent = parent;
        this.consolePos = consolePos;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    private int panelLeft() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    private int[] timeOptionBounds(int index) {
        return rowOptionBounds(index, TIMES.length, panelTop() + 34);
    }

    private int[] weatherOptionBounds(int index) {
        return rowOptionBounds(index, WEATHERS.length, panelTop() + 68);
    }

    private int[] rowOptionBounds(int index, int count, int top) {
        int totalWidth = PANEL_WIDTH - 16;
        int optionWidth = (totalWidth - (count - 1) * OPTION_GAP) / count;
        int left = panelLeft() + 8 + index * (optionWidth + OPTION_GAP);
        return new int[]{left, top, optionWidth, OPTION_HEIGHT};
    }

    private int[] confirmButtonBounds() {
        return new int[]{panelLeft() + 8, panelTop() + PANEL_HEIGHT - 24, ACTION_BUTTON_WIDTH, OPTION_HEIGHT};
    }

    private int[] cancelButtonBounds() {
        return new int[]{panelLeft() + PANEL_WIDTH - 8 - ACTION_BUTTON_WIDTH, panelTop() + PANEL_HEIGHT - 24, ACTION_BUTTON_WIDTH, OPTION_HEIGHT};
    }

    private boolean isInside(int mouseX, int mouseY, int[] bounds) {
        return mouseX >= bounds[0] && mouseX < bounds[0] + bounds[2] && mouseY >= bounds[1] && mouseY < bounds[1] + bounds[3];
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        guiGraphicsExtractor.fill(0, 0, this.width, this.height, COLOR_SCRIM);

        int left = panelLeft();
        int top = panelTop();
        guiGraphicsExtractor.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, COLOR_PANEL_BORDER);
        guiGraphicsExtractor.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, COLOR_PANEL_BG);

        int titleWidth = this.font.width(this.title);
        guiGraphicsExtractor.text(this.font, this.title, left + (PANEL_WIDTH - titleWidth) / 2, top + 6, COLOR_TITLE_TEXT, false);

        Component timeLabel = Component.translatable("gui.wtmod.combat_environment_picker.time");
        guiGraphicsExtractor.text(this.font, timeLabel, left + 8, top + 22, COLOR_SECTION_LABEL, false);
        for (int i = 0; i < TIMES.length; i++) {
            drawOption(guiGraphicsExtractor, timeOptionBounds(i), TIMES[i].displayName(), TIMES[i] == selectedTime, mouseX, mouseY);
        }

        Component weatherLabel = Component.translatable("gui.wtmod.combat_environment_picker.weather");
        guiGraphicsExtractor.text(this.font, weatherLabel, left + 8, top + 56, COLOR_SECTION_LABEL, false);
        for (int i = 0; i < WEATHERS.length; i++) {
            drawOption(guiGraphicsExtractor, weatherOptionBounds(i), WEATHERS[i].displayName(), WEATHERS[i] == selectedWeather, mouseX, mouseY);
        }

        drawButton(guiGraphicsExtractor, confirmButtonBounds(), Component.translatable("gui.wtmod.combat_environment_picker.confirm"), mouseX, mouseY);
        drawButton(guiGraphicsExtractor, cancelButtonBounds(), Component.translatable("gui.wtmod.combat_environment_picker.cancel"), mouseX, mouseY);
    }

    private void drawOption(GuiGraphicsExtractor guiGraphicsExtractor, int[] bounds, Component label, boolean selected, int mouseX, int mouseY) {
        int left = bounds[0];
        int top = bounds[1];
        int width = bounds[2];
        int height = bounds[3];
        boolean hovered = isInside(mouseX, mouseY, bounds);
        int color = selected ? COLOR_SELECTED_BG : (hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_IDLE);
        int textColor = selected ? COLOR_SELECTED_TEXT : COLOR_BUTTON_TEXT;
        guiGraphicsExtractor.fill(left - 1, top - 1, left + width + 1, top + height + 1, COLOR_PANEL_BORDER);
        guiGraphicsExtractor.fill(left, top, left + width, top + height, color);
        int textWidth = this.font.width(label);
        guiGraphicsExtractor.text(this.font, label, left + (width - textWidth) / 2, top + (height - 8) / 2, textColor, false);
    }

    private void drawButton(GuiGraphicsExtractor guiGraphicsExtractor, int[] bounds, Component label, int mouseX, int mouseY) {
        int left = bounds[0];
        int top = bounds[1];
        int width = bounds[2];
        int height = bounds[3];
        boolean hovered = isInside(mouseX, mouseY, bounds);
        guiGraphicsExtractor.fill(left - 1, top - 1, left + width + 1, top + height + 1, COLOR_PANEL_BORDER);
        guiGraphicsExtractor.fill(left, top, left + width, top + height, hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON_IDLE);
        int textWidth = this.font.width(label);
        guiGraphicsExtractor.text(this.font, label, left + (width - textWidth) / 2, top + (height - 8) / 2, COLOR_BUTTON_TEXT, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();

            for (int i = 0; i < TIMES.length; i++) {
                if (isInside(mouseX, mouseY, timeOptionBounds(i))) {
                    this.selectedTime = TIMES[i];
                    return true;
                }
            }

            for (int i = 0; i < WEATHERS.length; i++) {
                if (isInside(mouseX, mouseY, weatherOptionBounds(i))) {
                    this.selectedWeather = WEATHERS[i];
                    return true;
                }
            }

            if (isInside(mouseX, mouseY, confirmButtonBounds())) {
                ClientPacketDistributor.sendToServer(new CommonPayload.AddCombatPoolEntry(consolePos, selectedTime, selectedWeather));
                this.minecraft.setScreenAndShow(this.parent);
                return true;
            }

            if (isInside(mouseX, mouseY, cancelButtonBounds())) {
                this.minecraft.setScreenAndShow(this.parent);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}
