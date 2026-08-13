package com.JSUSHDX.WorldTriggerMod.blocks.menu;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, WorldTriggerMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AssemblyBenchMenu>>
            ASSEMBLY_BENCH_MENU = MENUS.register("assembly_bench_menu",
                    () -> IMenuTypeExtension.create(AssemblyBenchMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MotherTriggerMenu>>
            MOTHER_TRIGGER_MENU = MENUS.register("mother_trigger_menu",
                    () -> IMenuTypeExtension.create(MotherTriggerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<OperatorsTerminalMenu>>
            OPERATORS_TERMINAL_MENU = MENUS.register("operators_terminal_menu",
                    () -> IMenuTypeExtension.create(OperatorsTerminalMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
