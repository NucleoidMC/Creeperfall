package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.AlwaysFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.mixin.AbstractSkeletonEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.world.World;

public class CreeperfallSkeletonEntity extends SkeletonEntity {
	public CreeperfallSkeletonEntity(World world) {
		super(EntityType.SKELETON, world);
		((AbstractSkeletonEntityAccessor)this).setBowAttackGoal(
				new BowAttackGoal<>(this, 0.75D, 16, 90.0F)
		);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(6, new LookAtEntityGoal(this, CreeperEntity.class, 8.0F));
		this.goalSelector.add(6, new LookAroundGoal(this));
		this.targetSelector.add(
				1,
				new AlwaysFollowTargetGoal<>(
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
}
