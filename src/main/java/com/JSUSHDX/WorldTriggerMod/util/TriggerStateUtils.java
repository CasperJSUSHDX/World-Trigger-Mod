package com.JSUSHDX.WorldTriggerMod.util;

import com.JSUSHDX.WorldTriggerMod.data.ModDataAttachment;
import com.JSUSHDX.WorldTriggerMod.data.custom.TriggerStateData;
import net.minecraft.world.entity.player.Player;

public class TriggerStateUtils {
    public static boolean getState(Player player) { return player.getData(ModDataAttachment.TRIGGER_STATE_DATA).isMorph(); }

    public static void toggleState(Player player) {
        TriggerStateData data = player.getData(ModDataAttachment.TRIGGER_STATE_DATA);
        player.setData(ModDataAttachment.TRIGGER_STATE_DATA, data.setMorph(!data.isMorph()));
    }
}
