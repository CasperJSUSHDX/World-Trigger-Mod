package com.JSUSHDX.WorldTriggerMod;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import java.lang.reflect.Method;

public class TestReflection {
    public static void main(String[] args) {
        System.out.println("Methods in AbstractContainerScreen:");
        for (Method m : AbstractContainerScreen.class.getDeclaredMethods()) {
            System.out.println(m.getName() + " : " + java.util.Arrays.toString(m.getParameterTypes()));
        }
    }
}
