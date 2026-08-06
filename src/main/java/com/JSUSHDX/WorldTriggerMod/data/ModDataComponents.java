package com.JSUSHDX.WorldTriggerMod.data;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.data.records.HealthData;
import com.JSUSHDX.WorldTriggerMod.data.records.InventoryData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, WorldTriggerMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_ON =
            register("is_on",
                    booleanBuilder -> booleanBuilder.persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MODE =
            register("mode",
                    booleanBuilder -> booleanBuilder.persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT));


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HealthData>> HEALTH_DATA =
            register("health_data",
                    booleanBuilder -> booleanBuilder.persistent(HealthData.CODEC)
                            .networkSynchronized(HealthData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InventoryData>> INVENTORY_DATA =
            register("inventory_data",
                    builder -> builder.persistent(InventoryData.CODEC)
                            .networkSynchronized(InventoryData.STREAM_CODEC));

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                          UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
