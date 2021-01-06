package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.AlwaysFollowTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class CreeperfallSkeletonEntity extends SkeletonEntity {
	public CreeperfallSkeletonEntity(World world) {
		super(EntityType.SKELETON, world);
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
						livingEntity -> livingEntity instanceof CreeperEntity
				)
		);
	}

	@Override
	public void setOnFireFor(int seconds) {

	}
}
