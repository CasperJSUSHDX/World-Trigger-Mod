package com.JSUSHDX.WorldTriggerMod.datagen.custom;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class HybridItemModel {
    /**
     * A ModelTemplate for generating the 2D flat model variant.
     * Parent: minecraft:item/generated, uses the "_2d" suffixed texture.
     */
    public static final ModelTemplate HYBRID_ITEM = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("item/generated")),
            Optional.of("_2d"),
            TextureSlot.LAYER0
    );

    /**
     * Generates a hybrid item model that displays a 2D texture in GUI/ground/fixed/shelf contexts
     * and a 3D model when held in the player's hand or on the head.
     *
     * <p>This method:
     * <ol>
     *   <li>Generates a {@code <item_id>_2d} model JSON (flat, parent {@code minecraft:item/generated},
     *       texture {@code layer0} = {@code <namespace>:item/<item_id>_2d})</li>
     *   <li>References an existing hand-crafted {@code <item_id>_3d} model JSON
     *       (placed manually in {@code assets/<namespace>/models/item/<item_id>_3d.json})</li>
     *   <li>Creates an item definition that uses {@code minecraft:select} with {@code display_context}
     *       to switch between the 2D model for GUI/ground/fixed/on_shelf and the 3D model for everything else</li>
     * </ol>
     *
     * @param itemModels the item model generators instance
     * @param item       the item to generate models for
     */
    public static void generateHybridItem(ItemModelGenerators itemModels, Item item) {
        // 1. Generate the _2d flat model: models/item/<item_id>_2d.json
        //    Uses the "_2d" suffixed texture (textures/item/<item_id>_2d.png)
        Identifier flatModelId = HYBRID_ITEM.create(
                item,
                TextureMapping.layer0(TextureMapping.getItemTexture(item, "_2d")),
                itemModels.modelOutput
        );
        ItemModel.Unbaked flatModel = ItemModelUtils.plainModel(flatModelId);

        // 2. Reference the existing _3d model: models/item/<item_id>_3d.json
        //    This is a hand-crafted Blockbench model placed manually in the resources
        Identifier handModelId = ModelLocationUtils.getModelLocation(item, "_3d");
        ItemModel.Unbaked hybridModel = getUnbaked(handModelId, flatModel);

        // 4. Register the item definition
        itemModels.itemModelOutput.accept(item, hybridModel);
    }

    private static ItemModel.@NonNull Unbaked getUnbaked(Identifier handModelId, ItemModel.Unbaked flatModel) {
        ItemModel.Unbaked handModel = ItemModelUtils.plainModel(handModelId);

        // 3. Create a select-based item definition that dispatches by display context:
        //    - GUI, GROUND, FIXED, ON_SHELF → 2D flat model
        //    - Everything else (hand, head, etc.) → 3D model (fallback)
        return ItemModelUtils.select(
                new DisplayContext(),
                handModel, // fallback: 3D model for hand/head/etc.
                ItemModelUtils.when(
                        List.of(
                                ItemDisplayContext.GUI,
                                ItemDisplayContext.GROUND,
                                ItemDisplayContext.FIXED,
                                ItemDisplayContext.ON_SHELF
                        ),
                        flatModel // 2D model for GUI and similar contexts
                )
        );
    }
}
