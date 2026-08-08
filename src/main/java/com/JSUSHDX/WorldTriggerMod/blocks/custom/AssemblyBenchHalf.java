package com.JSUSHDX.WorldTriggerMod.blocks.custom;

import net.minecraft.util.StringRepresentable;

public enum AssemblyBenchHalf implements StringRepresentable {
    LEFT("left"),
    RIGHT("right");

    private final String name;

    AssemblyBenchHalf(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
