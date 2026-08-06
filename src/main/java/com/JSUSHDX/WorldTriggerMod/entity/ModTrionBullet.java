package com.JSUSHDX.WorldTriggerMod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ModTrionBullet extends AbstractArrow {
    private double speed;
    private double  power;
    private double maxDistance;
    private Vec3 startPos;
    private Vec3 storedDirection;

    protected ModTrionBullet(double totalTrion, double speedRate, double powerRate, double rangeRate, EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public ModTrionBullet(EntityType<? extends ModTrionBullet> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.startPos == null) {
            this.startPos = this.position();
        }

        if (this.startPos.distanceToSqr(this.position()) > this.maxDistance * this.maxDistance && !this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    public void setStoredDirection(Vec3 direction) {
        this.storedDirection = direction.normalize();
    }

    public Vec3 getStoredDirection() {
        return this.storedDirection;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putDouble("speed", this.speed);
        output.putDouble("maxDistance", this.maxDistance);
        if (this.storedDirection != null) {
            output.putDouble("storedDirX", this.storedDirection.x);
            output.putDouble("storedDirY", this.storedDirection.y);
            output.putDouble("storedDirZ", this.storedDirection.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.speed = input.getDoubleOr("speed", 0.0);
        this.maxDistance = input.getDoubleOr("maxDistance", 0.0);
        double dirX = input.getDoubleOr("storedDirX", 0.0);
        double dirY = input.getDoubleOr("storedDirY", 0.0);
        double dirZ = input.getDoubleOr("storedDirZ", 0.0);
        if (dirX != 0.0 || dirY != 0.0 || dirZ != 0.0) {
            this.storedDirection = new Vec3(dirX, dirY, dirZ).normalize();
        }
    }

    public void setupStats(double totalTrion, double speedRate, double powerRate, double rangeRate) {
        this.speed = getSpeed(totalTrion, speedRate);
        this.setBaseDamage(getPower(totalTrion, powerRate));
        this.maxDistance = getMaxDistance(totalTrion, rangeRate);

        this.pickup = Pickup.DISALLOWED;
        this.setNoGravity(true);
    }

    private double getSpeed(double totalTrion, double speedRate) {
        return totalTrion * speedRate;
    }

    private double getPower(double totalTrion, double powerRate) {
        return totalTrion * powerRate;
    }

    private double getMaxDistance(double totalTrion, double rangeRate) {
        return totalTrion * rangeRate;
    }
}
