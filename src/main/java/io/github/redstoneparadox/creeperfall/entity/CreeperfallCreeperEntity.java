package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CreeperfallCreeperEntity extends CreeperEntity {
	private double fallSpeedMultiplier;
	private int ticksUntilAutoIgnite = 3 * 20;
	private double positiveBound;
	private double negativeBound;

	public CreeperfallCreeperEntity(World world) {
		super(EntityType.CREEPER, world);
	}

	public CreeperfallCreeperEntity(World world, double fallSpeedMultiplier, double positiveBound, double negativeBound) {
		this(world);
		this.fallSpeedMultiplier = fallSpeedMultiplier;
		this.experiencePoints = 0;
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

			if (ticksUntilAutoIgnite > 0 && !getIgnited()) {
				ticksUntilAutoIgnite -= 1;
			}
			else {
				ignite();
			}
		}
		else {
			Vec3d velocity = getVelocity();
			Vec3d position = getPos();

			if (position.x <= negativeBound) velocity = new Vec3d(1.0, velocity.y, velocity.z);
			else if (position.x >= positiveBound) velocity = new Vec3d(-1.0, velocity.y, velocity.z);
			else velocity = new Vec3d(0.0, velocity.y, velocity.z);

			if (position.x <= negativeBound) velocity = new Vec3d(velocity.x, velocity.y, 1.0);
			else if (position.x >= positiveBound) velocity = new Vec3d(velocity.x, velocity.y, -1.0);
			else velocity = new Vec3d(velocity.x, velocity.y, 0.0);

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

	public boolean handleFallDamage(float f, float g) {
		return false;
	}
}
