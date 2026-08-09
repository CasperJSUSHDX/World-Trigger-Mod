package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.AssemblyBenchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import com.JSUSHDX.WorldTriggerMod.client.screen.base.BaseMachineScreen;

public class AssemblyBenchScreen extends BaseMachineScreen<AssemblyBenchMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/gui/assembly_bench_gui.png");

    public AssemblyBenchScreen(AssemblyBenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 236, 210);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title for the main portion (176 pixels wide)
        this.titleLabelX = (176 - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        
        // Move the inventory label down slightly so it sits between the line and the slots (Y=128)
        this.inventoryLabelY = 118;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        // Draw the default labels (like title and inventory label)
        super.extractLabels(guiGraphicsExtractor, mouseX, mouseY);

        // Draw our custom labels
        Component mainText = Component.translatable("gui.wtmod.main_triggers");
        Component subText = Component.translatable("gui.wtmod.sub_triggers");
        
        guiGraphicsExtractor.text(this.font, mainText, 8, 24, 0xFF404040, false);
        
        int subTextWidth = this.font.width(subText);
        guiGraphicsExtractor.text(this.font, subText, 176 - 8 - subTextWidth, 24, 0xFF404040, false);
        
        // Draw Dynamic Palette Category Headers
        for (AssemblyBenchMenu.CategoryLayout layout : this.menu.getCategoryLayouts()) {
            guiGraphicsExtractor.text(this.font, layout.name, layout.textX, layout.textY, 0xFF404040, false);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        // Draw slot backgrounds for dynamic palette slots
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        for (net.minecraft.world.inventory.Slot slot : this.menu.slots) {
            if (this.menu.isPaletteSlot(slot)) {
                // Copy the empty slot background from (25, 37) of our texture
                guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED,
                        TEXTURE,
                        leftPos + slot.x - 1, topPos + slot.y - 1,
                        25.0f, 37.0f,
                        18, 18,
                        256, 256);
            }
        }
    }
}
