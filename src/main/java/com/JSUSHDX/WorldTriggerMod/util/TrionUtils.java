package com.JSUSHDX.WorldTriggerMod.util;

import com.JSUSHDX.WorldTriggerMod.data.ModDataAttachment;
import com.JSUSHDX.WorldTriggerMod.data.custom.TrionData;
import net.minecraft.world.entity.player.Player;

public class TrionUtils {
    public static float getTrion(Player player) {
        return player.getData(ModDataAttachment.TRION_DATA).trion();
    }

    public static float getMaxTrion(Player player) {
        return player.getData(ModDataAttachment.TRION_DATA).maxTrion();
    }

    public static void addTrion(Player player, float amount) {
        TrionData data = player.getData(ModDataAttachment.TRION_DATA);
        player.setData(ModDataAttachment.TRION_DATA, data.updateTrion(data.trion() + amount));
    }

    /**
     * Try to consume player's trion
     * @return True when success otherwise False
     */
    public static boolean consumeTrion(Player player, float amount) {
        TrionData data = player.getData(ModDataAttachment.TRION_DATA);
        if (data.trion() >= amount) {
            player.setData(ModDataAttachment.TRION_DATA, data.updateTrion(data.trion() - amount));
            return true;
        }
        return false;
    }
}
