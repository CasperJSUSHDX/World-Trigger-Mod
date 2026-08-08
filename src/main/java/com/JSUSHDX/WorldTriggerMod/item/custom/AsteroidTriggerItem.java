package com.JSUSHDX.WorldTriggerMod.item.custom;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.entity.ModEntities;
import com.JSUSHDX.WorldTriggerMod.entity.ModTrionBullet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.JSUSHDX.WorldTriggerMod.WorldTriggerMod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@EventBusSubscriber(modid = WorldTriggerMod.MODID)
public class AsteroidTriggerItem extends Item {

    private static final Map<UUID, FiringState> activeShooters = new ConcurrentHashMap<>();
    private static final Map<UUID, List<ModTrionBullet>> placedBulletsMap = new ConcurrentHashMap<>();

    private static class FiringState {
        int bulletsLeft;
        int ticksUntilNext;

        FiringState(int bulletsLeft, int ticksUntilNext) {
            this.bulletsLeft = bulletsLeft;
            this.ticksUntilNext = ticksUntilNext;
        }
    }

    public AsteroidTriggerItem(Properties properties) {
        super(properties);
    }

    public record modeMethods(String langId, BiConsumer<Level, Player> method) {};
    public static final Map<Integer, modeMethods> modeMap = new HashMap<>(Map.of(
            0, new modeMethods("tooltip.wtmod.asteroid_mode0", (level, player)->{
                // Start firing 8 bullets, 1 immediate, 7 more to come, interval of 4 ticks (0.2s)
                shootBullet(level, player);
                activeShooters.put(player.getUUID(), new FiringState(7, 2));
            }),
            4, new modeMethods("tooltip.wtmod.asteroid_mode4", AsteroidTriggerItem::placeBullets)
    ));

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            Integer mode = player.getMainHandItem().getOrDefault(ModDataComponents.MODE, 0);
            if (mode == null) mode = 0; // Default mode

            modeMethods methods = modeMap.get(mode);
            if (methods != null) {
                methods.method.accept(level, player);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        FiringState state = activeShooters.get(playerId);

        if (state != null) {
            state.ticksUntilNext--;
            if (state.ticksUntilNext <= 0) {
                shootBullet(player.level(), player);
                state.bulletsLeft--;
                
                if (state.bulletsLeft <= 0) {
                    activeShooters.remove(playerId);
                } else {
                    state.ticksUntilNext = 4; // Reset timer to 4 ticks (0.2 seconds)
                }
            }
        }
    }

    public static void placeBullets(Level level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 bulletPos = player.position().add(0, 0.5, 0);
        for (int i = 0; i < 8; ++i) {
            ModTrionBullet bullet = new ModTrionBullet(ModEntities.TRION_BULLET.get(), level);
            Vec3 randomOffset = new Vec3(random.nextDouble() * 0.5, random.nextDouble() * 0.5, random.nextDouble() * 0.5);
            bullet.setupStats(20.0d, 0.0d, 0.7d, 2.5d);

            bullet.setPos(bulletPos.add(randomOffset));
            bullet.setStoredDirection(player.getLookAngle());

            level.addFreshEntity(bullet);

            placedBulletsMap.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(bullet);
        }
    }

    public static void triggerPlacedBullets(Player player) {
        UUID playerId = player.getUUID();
        List<ModTrionBullet> bullets = placedBulletsMap.get(playerId);

        if (bullets != null) {
            for (ModTrionBullet bullet : bullets) {
                if (!bullet.isRemoved()) {
                    Vec3 dir = bullet.getStoredDirection();
                    if (dir != null) {
                        bullet.shoot(dir.x, dir.y, dir.z, 1.5f, 0.0f);
                    }
                }
            }
            // Clear bullets after triggering to prevent memory leaks
            placedBulletsMap.remove(playerId);
        }
    }

    public static void shootBullet(Level level, Player player) {
        Vec3 playerPos = player.position();
        RandomSource random = level.getRandom();

        Vec3 randomOffset = new Vec3(random.nextDouble() * 0.5, random.nextDouble() * 0.5, random.nextDouble() * 0.5);

        ModTrionBullet bullet = new ModTrionBullet(ModEntities.TRION_BULLET.get(), level);

        bullet.setupStats(20.0d, 0.001d, 0.7d, 2.5d);
        bullet.setPos(playerPos.add(0, 2, 0).add(randomOffset));
        bullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);

        level.addFreshEntity(bullet);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        int mode = itemStack.getOrDefault(ModDataComponents.MODE, 0);
        modeMethods methods = modeMap.get(mode);
        if (methods != null) {
            Component msg = Component.translatable("tooltip.wtmod.mode", Component.translatable(methods.langId)).withColor(TextColor.WHITE);
            builder.accept(msg);
        }
    }
}
