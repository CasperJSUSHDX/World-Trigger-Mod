package com.JSUSHDX.WorldTriggerMod.blocks.entity;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WorldTriggerMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AssemblyBenchBlockEntity>>
            ASSEMBLY_BENCH_BE = BLOCK_ENTITIES.register("assembly_bench_be",
                    () -> new BlockEntityType<>(AssemblyBenchBlockEntity::new, ModBlocks.ASSEMBLY_BENCH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotherTriggerBlockEntity>>
            MOTHER_TRIGGER_BE = BLOCK_ENTITIES.register("mother_trigger_be",
                    () -> new BlockEntityType<>(MotherTriggerBlockEntity::new, ModBlocks.MOTHER_TRIGGER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OperatorsTerminalBlockEntity>>
            OPERATORS_TERMINAL_BE = BLOCK_ENTITIES.register("operators_terminal_be",
                    () -> new BlockEntityType<>(OperatorsTerminalBlockEntity::new, ModBlocks.OPERATORS_TERMINAL.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
