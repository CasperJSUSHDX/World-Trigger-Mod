package com.JSUSHDX.WorldTriggerMod.network;

import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;
import com.JSUSHDX.WorldTriggerMod.blocks.entity.OperatorsTerminalBlockEntity;
import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.item.custom.AsteroidTriggerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

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

    public record SetPlayerSlot(int slot) implements CustomPacketPayload {
        // Payload ID
        public static final CustomPacketPayload.Type<SetPlayerSlot> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "set_player_slot"));

        public static final StreamCodec<ByteBuf, SetPlayerSlot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SetPlayerSlot::slot,
                SetPlayerSlot::new
        );

        public static void handler(final SetPlayerSlot data, final IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();

                player.getInventory().setSelectedSlot(data.slot());
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RemoveTerminalEntry(BlockPos pos, UUID ownerId) implements CustomPacketPayload {
        // Payload ID
        public static final CustomPacketPayload.Type<RemoveTerminalEntry> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WorldTriggerMod.MODID, "remove_terminal_entry"));

        public static final StreamCodec<ByteBuf, RemoveTerminalEntry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, RemoveTerminalEntry::pos,
                UUIDUtil.STREAM_CODEC, RemoveTerminalEntry::ownerId,
                RemoveTerminalEntry::new
        );

        public static void handler(final RemoveTerminalEntry data, final IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();

                if (player.level().getBlockEntity(data.pos()) instanceof OperatorsTerminalBlockEntity blockEntity) {
                    blockEntity.removeEntry(data.ownerId());
                }
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
