package com.JSUSHDX.WorldTriggerMod.client.screen.base;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseMachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected final Identifier backgroundTexture;

    public BaseMachineScreen(T menu, Inventory playerInventory, Component title, Identifier backgroundTexture, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
        this.backgroundTexture = backgroundTexture;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94; // Standard offset for player inventory
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, x, y, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }
}
