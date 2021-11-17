package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CreeperfallCreeperEntity extends CreeperEntity {
	private final double multX;
	private final double multZ;
	private double fallSpeedMultiplier;
	private int ticksUntilAutoIgnite = 3 * 20;

	public CreeperfallCreeperEntity(World world, double fallSpeedMultiplier, double multX, double multZ) {
		super(EntityType.CREEPER, world);
		this.fallSpeedMultiplier = fallSpeedMultiplier;
		this.experiencePoints = 0;
		this.multX = multX;
		this.multZ = multZ;
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new CreeperIgniteGoal(this));
		this.goalSelector.add(3, new FleeEntityGoal<>(this, OcelotEntity.class, 6.0F, 1.0D, 1.2D));
		this.goalSelector.add(3, new FleeEntityGoal<>(this, CatEntity.class, 6.0F, 1.0D, 1.2D));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 128.0F, 1.0f));
		this.goalSelector.add(6, new LookAtEntityGoal(this, SkeletonEntity.class, 128.0f, 1.0f));
		this.targetSelector.add(1, new CreeperfallFollowTargetGoal<>(
						this,
						PlayerEntity.class,
						1,
						true,
						true,
						livingEntity -> true
				)
		);
		this.targetSelector.add(2, new CreeperfallFollowTargetGoal<>(
				this,
				SkeletonEntity.class,
				1,
				true,
				true,
				livingEntity -> true
				)
		);
	}

	@Override
	public void setMovementSpeed(float movementSpeed) {
		super.setMovementSpeed(movementSpeed * 1.15f);
	}

	@Override
	public void tick() {
		if (isOnGround()) {
			setInvulnerable(true);

			if (ticksUntilAutoIgnite > 0 && !this.isIgnited()) {
				ticksUntilAutoIgnite -= 1;
			}
			else {
				ignite();
			}
		}
		else {
			Vec3d velocity = getVelocity();

			double value = ((double) this.age) / 10 + this.getId();

			velocity = new Vec3d(velocity.x + Math.sin(value) * this.multX, velocity.y, velocity.z + Math.cos(value) * this.multZ);

			setVelocity(velocity);
		}

		if (!isInvulnerable()) {
			Vec3d velocity = getVelocity();
			setVelocity(velocity.multiply(1.0, fallSpeedMultiplier, 1.0));
		}

		if (getY() <= 0) {
			kill();
		}
		super.tick();
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}
}
