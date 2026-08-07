package com.JSUSHDX.WorldTriggerMod.entity.custom;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.entity.ModEntities;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class ShieldEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = 
            SynchedEntityData.defineId(ShieldEntity.class, EntityDataSerializers.INT);

    // UUID for server side
    private UUID ownerUUID;

    public ShieldEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public ShieldEntity(Level level, Player owner) {
        super(ModEntities.SHIELD_ENTITY.get(), level);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DATA_OWNER_ID, owner.getId());
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();

        Player player = null;

        if (!this.level().isClientSide()) {
            // Server 端：依賴 UUID 準確找人
            if (this.ownerUUID == null) {
                this.discard();
                return;
            }
            player = this.level().getPlayerByUUID(this.ownerUUID);
            
            // 確保 Client 的 ID 是一致的
            if (player != null && this.entityData.get(DATA_OWNER_ID) != player.getId()) {
                this.entityData.set(DATA_OWNER_ID, player.getId());
            }
        } else {
            // Client 端：直接利用 Entity ID 快速找人，避免 UUID 查找
            int ownerId = this.entityData.get(DATA_OWNER_ID);
            if (ownerId != -1) {
                Entity entity = this.level().getEntity(ownerId);
                if (entity instanceof Player) {
                    player = (Player) entity;
                }
            }
        }

        if (player == null || player.isRemoved() || !player.isAlive()) {
            if (!this.level().isClientSide()) {
                this.discard();
            }
            return;
        }

        // Discard conditions 2: player turn off the trigger
        ItemStack itemstack = player.getMainHandItem();
        if (itemstack.is(ModItems.SHIELD_TRIGGER.get())) {
            Boolean isOn = itemstack.getOrDefault(ModDataComponents.IS_ON, false);
            if (!isOn && !this.level().isClientSide()) { this.discard(); }
        }

        // Calculate position slightly in front of the player
        Vec3 lookAngle = player.getLookAngle();
        double distance = 1.5; 
        
        double targetX = player.getX() + lookAngle.x * distance;
        double targetY = player.getEyeY() - 0.5 + lookAngle.y * distance; 
        double targetZ = player.getZ() + lookAngle.z * distance;

        // 設定座標與旋轉
        this.setPosRaw(targetX, targetY, targetZ); 
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.yRotO = player.yRotO;
        this.xRotO = player.xRotO;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        Optional<String> ownerStr = input.getString("Owner");
        if (ownerStr.isPresent() && !ownerStr.get().isEmpty()) {
            this.ownerUUID = UUID.fromString(ownerStr.get());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.ownerUUID != null) {
            output.putString("Owner", this.ownerUUID.toString());
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        if (damageSource.getEntity() != null && this.ownerUUID != null && damageSource.getEntity().getUUID().equals(this.ownerUUID)) {
            return false;
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F + serverLevel.getRandom().nextFloat() * 0.4F);

        if (damageSource.getDirectEntity() instanceof Projectile) {
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
        }

        return true;
    }
}
