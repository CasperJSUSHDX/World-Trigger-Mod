package com.JSUSHDX.WorldTriggerMod.tags;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        private static TagKey<Block> createTag(String id) {
            return BlockTags.create(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, id));
        }
    }

    public static class Items {
        public static final TagKey<Item> WEAPON_TRIGGERS = createTag("weapon_triggers");
        public static final TagKey<Item> DEFENSE_TRIGGERS = createTag("defense_triggers");
        public static final TagKey<Item> OPTIONAL_TRIGGERS = createTag("optional_triggers");

        private static TagKey<Item> createTag(String id) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, id));
        }
    }
}
