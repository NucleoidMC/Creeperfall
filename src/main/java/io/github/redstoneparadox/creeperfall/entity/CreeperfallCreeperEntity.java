package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CreeperfallCreeperEntity extends CreeperEntity {
	private double fallSpeedMultiplier;
	private int ticksUntilAutoIgnite = 3 * 20;

	public CreeperfallCreeperEntity(World world) {
		super(EntityType.CREEPER, world);
	}

	public CreeperfallCreeperEntity(World world, double fallSpeedMultiplier) {
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
		this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8D));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(6, new LookAroundGoal(this));
		this.targetSelector.add(1, new FollowTargetGoal<>(this, PlayerEntity.class, true));
		this.goalSelector.add(6, new LookAtEntityGoal(this, SkeletonEntity.class, 8.0F));
		this.targetSelector.add(1, new FollowTargetGoal<>(this, SkeletonEntity.class, true));
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
