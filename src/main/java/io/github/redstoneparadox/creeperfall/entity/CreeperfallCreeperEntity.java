package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class CreeperfallCreeperEntity extends Creeper {
	private final double multX;
	private final double multZ;
	private double fallSpeedMultiplier;
	private int ticksUntilAutoIgnite = 3 * 20;

	public CreeperfallCreeperEntity(Level level, double fallSpeedMultiplier, double multX, double multZ) {
		super(EntityTypes.CREEPER, level);
		this.fallSpeedMultiplier = fallSpeedMultiplier;
		this.xpReward = 0;
		this.multX = multX;
		this.multZ = multZ;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new SwellGoal(this));
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0D, 1.2D));
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0D, 1.2D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 128.0F, 1.0f));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Skeleton.class, 128.0f, 1.0f));
		this.targetSelector.addGoal(1, new CreeperfallFollowTargetGoal<>(
						this,
						Player.class,
						1,
						true,
						true,
						(livingEntity, world) -> true
				)
		);
		this.targetSelector.addGoal(2, new CreeperfallFollowTargetGoal<>(
				this,
				Skeleton.class,
				1,
				true,
				true,
				(livingEntity, world) -> true
				)
		);
	}

	@Override
	public void setSpeed(float movementSpeed) {
		super.setSpeed(movementSpeed * 1.15f);
	}

	@Override
	public void tick() {
		if (onGround()) {
			setInvulnerable(true);

			if (ticksUntilAutoIgnite > 0 && !this.isIgnited()) {
				ticksUntilAutoIgnite -= 1;
			}
			else {
				ignite();
			}
		}
		else {
			Vec3 velocity = getDeltaMovement();

			// double value = ((double) this.age) / 10 + this.getId();

			// velocity = new Vec3d(velocity.x + Math.sin(value) * this.multX, velocity.y, velocity.z + Math.cos(value) * this.multZ);

			velocity = new Vec3(0, velocity.y, 0);

			setDeltaMovement(velocity);
		}

		if (!isInvulnerable()) {
			Vec3 velocity = getDeltaMovement();
			setDeltaMovement(velocity.multiply(0, fallSpeedMultiplier, 0));
		}

		if (getY() <= 0) {
			kill((ServerLevel) this.level());
		}
		super.tick();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
		return false;
	}
}
