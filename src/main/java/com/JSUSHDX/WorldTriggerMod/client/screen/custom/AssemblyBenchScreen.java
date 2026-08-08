package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.AssemblyBenchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AssemblyBenchScreen extends AbstractContainerScreen<AssemblyBenchMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/gui/assembly_bench_gui.png");

    public AssemblyBenchScreen(AssemblyBenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 210);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title "Assembly bench" in the top middle
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        // Move the inventory label down slightly so it sits between the line and the slots (Y=128)
        this.inventoryLabelY = 118;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        // Draw the default labels (like title and inventory label)
        super.extractLabels(guiGraphicsExtractor, mouseX, mouseY);

        // Draw our custom labels (color must include 0xFF alpha channel in 1.21.3)
        Component mainText = Component.translatable("gui.wtmod.main_triggers");
        Component subText = Component.translatable("gui.wtmod.sub_triggers");
        
        guiGraphicsExtractor.text(this.font, mainText, 8, 24, 0xFF404040, false);
        
        int subTextWidth = this.font.width(subText);
        guiGraphicsExtractor.text(this.font, subText, this.imageWidth - 8 - subTextWidth, 24, 0xFF404040, false);
    }
}
