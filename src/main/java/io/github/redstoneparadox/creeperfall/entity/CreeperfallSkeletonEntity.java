package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.entity.ai.goal.LookUpAtEntityGoal;
import io.github.redstoneparadox.creeperfall.mixin.AbstractSkeletonAccessor;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.level.Level;

public class CreeperfallSkeletonEntity extends Skeleton {
	private int timeToDespawn = 30 * 20;

	public CreeperfallSkeletonEntity(Level level) {
		super(EntityTypes.SKELETON, level);
		((AbstractSkeletonAccessor)this).setBowGoal(
				new CreeperfallBowAttackGoal(this, 1.5D, 1, 64.0F)
		);
	}

	@Override
	public void tick() {
		super.tick();

		if (timeToDespawn <= 0) {
			remove(RemovalReason.DISCARDED);
		} else {
			timeToDespawn -= 1;
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(6, new LookUpAtEntityGoal(this, Creeper.class, 64.0F));
		this.targetSelector.addGoal(
				1,
				new CreeperfallFollowTargetGoal<>(
						this,
						LivingEntity.class,
						10,
						true,
						false,
						(livingEntity, world) -> livingEntity instanceof Creeper && !livingEntity.onGround()
				)
		);
	}

	@Override
	public void igniteForTicks(int ticks) {

	}

	static class CreeperfallBowAttackGoal extends RangedBowAttackGoal<AbstractSkeleton> {
		public CreeperfallBowAttackGoal(AbstractSkeleton actor, double speed, int attackInterval, float range) {
			super(actor, speed, attackInterval, range);
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse() && isHoldingBow();
		}
	}
}
