package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.entity.ai.goal.LookUpAtEntityGoal;
import io.github.redstoneparadox.creeperfall.mixin.AbstractSkeletonEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.world.World;

public class CreeperfallSkeletonEntity extends SkeletonEntity {
	private int timeToDespawn = 30 * 20;

	public CreeperfallSkeletonEntity(World world) {
		super(EntityType.SKELETON, world);
		((AbstractSkeletonEntityAccessor)this).setBowAttackGoal(
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
	protected void initGoals() {
		this.goalSelector.add(6, new LookUpAtEntityGoal(this, CreeperEntity.class, 64.0F));
		this.targetSelector.add(
				1,
				new CreeperfallFollowTargetGoal<>(
						this,
						LivingEntity.class,
						10,
						true,
						false,
						livingEntity -> livingEntity instanceof CreeperEntity && !livingEntity.isOnGround()
				)
		);
	}

	@Override
	public void setOnFireFor(int seconds) {

	}

	static class CreeperfallBowAttackGoal extends BowAttackGoal<AbstractSkeletonEntity> {
		public CreeperfallBowAttackGoal(AbstractSkeletonEntity actor, double speed, int attackInterval, float range) {
			super(actor, speed, attackInterval, range);
		}

		@Override
		public boolean shouldContinue() {
			return this.canStart() && isHoldingBow();
		}
	}
}
