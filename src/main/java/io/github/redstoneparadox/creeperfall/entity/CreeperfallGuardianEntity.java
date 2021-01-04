package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.AlwaysFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.mixin.GuardianEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Objects;

public class CreeperfallGuardianEntity extends GuardianEntity {
	public CreeperfallGuardianEntity(World world) {
		super(EntityType.GUARDIAN, world);
	}

	@Override
	public int getWarmupTime() {
		return 2;
	}

	@Override
	protected void initGoals() {
		this.wanderGoal = new WanderAroundGoal(this, 0.0D, 0);
		this.goalSelector.add(4, new FireBeamGoal(this));
		this.goalSelector.add(8, new LookAtEntityGoal(this, CreeperEntity.class, 256.0F));
		this.wanderGoal.setControls(EnumSet.of(Goal.Control.LOOK));
		this.targetSelector.add(
				1,
				new AlwaysFollowTargetGoal<>(
						this,
						LivingEntity.class,
						10,
						true,
						false,
						livingEntity -> livingEntity instanceof CreeperEntity
				)
		);
	}

	@Override
	public void setMovementSpeed(float movementSpeed) {

	}

	@Override
	public void setVelocity(Vec3d velocity) {

	}

	@Override
	public void tick() {
		super.tick();
		float x = 0.5f;
		float z = 0.5f;

		setPos(x, getY(), z);
		updatePosition(x, getY(), z);

		prevX = x;
		prevZ = z;
	}

	static class FireBeamGoal extends Goal {
		private final CreeperfallGuardianEntity guardian;
		private int beamTicks;

		public FireBeamGoal(CreeperfallGuardianEntity guardianEntity) {
			this.guardian = guardianEntity;
			this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		}

		public boolean canStart() {
			LivingEntity livingEntity = this.guardian.getTarget();
			return livingEntity != null && livingEntity.isAlive();
		}

		public boolean shouldContinue() {
			return super.shouldContinue() && (this.guardian.getTarget().getY() <= 75);
		}

		public void start() {
			this.beamTicks = 0;
			this.guardian.getNavigation().stop();
			this.guardian.getLookControl().lookAt(Objects.requireNonNull(this.guardian.getTarget()), 90.0F, 90.0F);
			this.guardian.velocityDirty = true;
		}

		public void stop() {
			((GuardianEntityAccessor)this.guardian).invokeSetBeamTarget(0);
			this.guardian.setTarget(null);
			this.guardian.wanderGoal.ignoreChanceOnce();
		}

		public void tick() {
			LivingEntity livingEntity = this.guardian.getTarget();
			this.guardian.getNavigation().stop();
			this.guardian.getLookControl().lookAt(livingEntity, 90.0F, 90.0F);
			if (!this.guardian.canSee(livingEntity)) {
				this.guardian.setTarget(null);
			} else {
				++this.beamTicks;
				if (this.beamTicks == 0) {
					((GuardianEntityAccessor)this.guardian).invokeSetBeamTarget(this.guardian.getTarget().getEntityId());
					if (!this.guardian.isSilent()) {
						this.guardian.world.sendEntityStatus(this.guardian, (byte)21);
					}
				} else if (this.beamTicks >= this.guardian.getWarmupTime()) {
					float f = 1.0F;

					livingEntity.damage(DamageSource.magic(this.guardian, this.guardian), f);
					this.guardian.setTarget(null);
				}

				super.tick();
			}
		}
	}
}
