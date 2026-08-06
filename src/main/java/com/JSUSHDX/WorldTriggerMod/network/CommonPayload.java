package com.JSUSHDX.WorldTriggerMod.network;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.item.custom.AsteroidTriggerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CommonPayload {
    public record ChangeMode(int mode) implements CustomPacketPayload {
        // Payload ID
        public static final CustomPacketPayload.Type<ChangeMode> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "change_mode"));

        // Codec
        public static final StreamCodec<ByteBuf, ChangeMode> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ChangeMode::mode,
                ChangeMode::new
        );

        public static void handler(final ChangeMode data, final IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();

                Integer mode = data.mode();
                player.getMainHandItem().set(ModDataComponents.MODE, mode);
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    };

    public record TriggerPlacedBullet() implements CustomPacketPayload {
        public static final TriggerPlacedBullet INSTANCE = new TriggerPlacedBullet();

        // Payload ID
        public static final CustomPacketPayload.Type<TriggerPlacedBullet> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "trigger_placed_bullet"));

        public static final StreamCodec<ByteBuf, TriggerPlacedBullet> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        public static void handler(final TriggerPlacedBullet data, final IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();

                AsteroidTriggerItem.triggerPlacedBullets(player);
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
