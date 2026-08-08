package com.JSUSHDX.WorldTriggerMod.client.screen.custom;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.menu.AssemblyBenchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import com.JSUSHDX.WorldTriggerMod.client.screen.base.BaseMachineScreen;

public class AssemblyBenchScreen extends BaseMachineScreen<AssemblyBenchMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "textures/gui/assembly_bench_gui.png");

    public AssemblyBenchScreen(AssemblyBenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 176, 210);
    }

    @Override
    protected void init() {
        super.init();
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
        guiGraphicsExtractor.text(this.font, subText, this.imageWidth - 8 - subTextWidth, 24, 0xFF404040, false);
    }
}
