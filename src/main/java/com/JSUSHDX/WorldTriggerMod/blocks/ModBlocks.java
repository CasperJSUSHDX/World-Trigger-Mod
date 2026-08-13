package com.JSUSHDX.WorldTriggerMod.blocks;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.MotherTriggerBlock;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.OperatorsTerminalBlock;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.RecallBedBlock;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import com.JSUSHDX.WorldTriggerMod.blocks.custom.AssemblyBenchBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(WorldTriggerMod.MODID);

    public static final DeferredBlock<AssemblyBenchBlock> ASSEMBLY_BENCH = registerBlock("assembly_bench",
            properties -> new AssemblyBenchBlock(
                    properties.strength(4f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<RecallBedBlock> RECALL_BED = registerBlock("recall_bed",
            properties -> new RecallBedBlock(
                    properties.strength(4f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<MotherTriggerBlock> MOTHER_TRIGGER = registerBlock("mother_trigger",
            properties -> new MotherTriggerBlock(
                    properties.strength(6f).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> 8)));

    public static final DeferredBlock<OperatorsTerminalBlock> OPERATORS_TERMINAL = registerBlock("operators_terminal",
            properties -> new OperatorsTerminalBlock(
                    properties.strength(5f).requiresCorrectToolForDrops().noOcclusion()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);

        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name,
                (properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix())));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
