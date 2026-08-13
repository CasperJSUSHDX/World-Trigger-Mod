package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.MotherTriggerMenu;
import com.JSUSHDX.WorldTriggerMod.client.screen.base.BaseMachineScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MotherTriggerScreen extends BaseMachineScreen<MotherTriggerMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/gui/mother_trigger_gui.png");

    public MotherTriggerScreen(MotherTriggerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 176, 210);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 118;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        super.extractLabels(guiGraphicsExtractor, mouseX, mouseY);

        Component rangeText = Component.translatable("gui.wtmod.mother_trigger_range", (int) this.menu.getBlockEntity().getRange());
        int textWidth = this.font.width(rangeText);
        guiGraphicsExtractor.text(this.font, rangeText, (176 - textWidth) / 2, 108, 0xFF404040, false);
    }
}
