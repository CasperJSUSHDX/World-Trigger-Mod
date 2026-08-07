package com.JSUSHDX.WorldTriggerMod.entity.custom;

import com.JSUSHDX.WorldTriggerMod.data.ModDataComponents;
import com.JSUSHDX.WorldTriggerMod.entity.ModEntities;
import com.JSUSHDX.WorldTriggerMod.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class ShieldEntity extends Entity {
    private UUID owner;

    // Default constructor required by EntityType registration
    public ShieldEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    // Convenience constructor for spawning
    public ShieldEntity(Level level, Player owner) {
        super(ModEntities.SHIELD_ENTITY.get(), level);
        this.owner = owner.getUUID();
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.owner == null) {
            if (!this.level().isClientSide()) {
                this.discard();
            }
            return;
        }

        Player player = this.level().getPlayerByUUID(this.owner);

        // Discard conditions 1: player offline, dead, or we can add item check later
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
        double distance = 1.5; // Blocks in front of player
        
        double targetX = player.getX() + lookAngle.x * distance;
        // Adjust Y to be roughly at chest/eye level
        double targetY = player.getEyeY() - 0.5 + lookAngle.y * distance; 
        double targetZ = player.getZ() + lookAngle.z * distance;

        // Teleport the entity to the target position and match player's rotation
        this.teleportTo(targetX, targetY, targetZ);
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.yRotO = player.yRotO;
        this.xRotO = player.xRotO;
    }

    /**
     * Ensure this entity can be hit by projectile or player
     * */
    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        Optional<String> ownerStr = input.getString("Owner");
        if (ownerStr.isPresent() && !ownerStr.get().isEmpty()) {
            this.owner = UUID.fromString(ownerStr.get());
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        if (this.owner != null) {
            output.putString("Owner", this.owner.toString());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Here you can add data to sync to the client (e.g. current health of the shield)
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        // Prevent damage from the owner
        if (damageSource.getEntity() != null && damageSource.getEntity().getUUID().equals(this.owner)) {
            return false;
        }

        // Play shield block sound
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F + serverLevel.getRandom().nextFloat() * 0.4F);

        // If it's a projectile, we can show some particles where it hit
        if (damageSource.getDirectEntity() instanceof Projectile) {
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
        }

        // TODO: Subtract from Trion/Health. For now, it just absorbs the damage.
        // If Health <= 0, then this.discard() and play break sound (SoundEvents.SHIELD_BREAK).

        // Return true to indicate the damage was successfully handled/intercepted
        return true;
    }
}
