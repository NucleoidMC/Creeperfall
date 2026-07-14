package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.mixin.GuardianAccessor;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Objects;

public class CreeperfallGuardianEntity extends Guardian {
	private int timeToDespawn = 30 * 20;

	public CreeperfallGuardianEntity(Level level) {
		super(EntityTypes.GUARDIAN, level);
	}

	@Override
	public int getAttackDuration() {
		return 2;
	}

	@Override
	protected void registerGoals() {
		this.randomStrollGoal = new RandomStrollGoal(this, 0.0D, 0);
		this.goalSelector.addGoal(4, new FireBeamGoal(this));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Creeper.class, 256.0F));
		this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.LOOK));
		this.targetSelector.addGoal(
				1,
				new CreeperfallFollowTargetGoal<>(
						this,
						LivingEntity.class,
						10,
						true,
						false,
						(livingEntity, world) -> livingEntity instanceof Creeper
				)
		);
	}

	@Override
	public void setSpeed(float movementSpeed) {

	}

	@Override
	public void setDeltaMovement(Vec3 velocity) {

	}

	@Override
	public void tick() {
		super.tick();
		float x = 0.5f;
		float z = 0.5f;

		setPosRaw(x, getY(), z);
		absSnapTo(x, getY(), z);

		xo = x;
		zo = z;

		timeToDespawn -= 1;

		if (timeToDespawn <= 0 && !this.isRemoved()) {
			this.remove(RemovalReason.DISCARDED);
		}
	}

	static class FireBeamGoal extends Goal {
		private final CreeperfallGuardianEntity guardian;
		private int beamTicks;

		public FireBeamGoal(CreeperfallGuardianEntity guardianEntity) {
			this.guardian = guardianEntity;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		public boolean canUse() {
			LivingEntity livingEntity = this.guardian.getTarget();
			return livingEntity != null && livingEntity.isAlive();
		}

		public boolean canContinueToUse() {
			return super.canContinueToUse() && (this.guardian.getTarget().getY() <= 75);
		}

		public void start() {
			this.beamTicks = -1;
			this.guardian.getNavigation().stop();
			this.guardian.getLookControl().setLookAt(Objects.requireNonNull(this.guardian.getTarget()), 90.0F, 90.0F);
			this.guardian.needsSync = true;
		}

		public void stop() {
			((GuardianAccessor)this.guardian).invokeSetActiveAttackTarget(0);
			this.guardian.setTarget(null);
			this.guardian.randomStrollGoal.trigger();
		}

		public void tick() {
			LivingEntity livingEntity = this.guardian.getTarget();
			this.guardian.getNavigation().stop();
			this.guardian.getLookControl().setLookAt(livingEntity, 90.0F, 90.0F);
			if (!this.guardian.hasLineOfSight(livingEntity)) {
				this.guardian.setTarget(null);
			} else {
				++this.beamTicks;
				if (this.beamTicks == 0) {
					((GuardianAccessor)this.guardian).invokeSetActiveAttackTarget(this.guardian.getTarget().getId());
					if (!this.guardian.isSilent()) {
						this.guardian.level().broadcastEntityEvent(this.guardian, (byte)21);
					}
				} else if (this.beamTicks >= this.guardian.getAttackDuration()) {
					float f = 1.0F;

					// TODO: Fix if guardians are re-added
					// livingEntity.damage(new DamageSource(), f);
					this.guardian.setTarget(null);
				}

				super.tick();
			}
		}
	}
}
