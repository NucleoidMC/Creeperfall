package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class CreeperfallShulkerEntity extends ShulkerEntity {
	public CreeperfallShulkerEntity(EntityType<? extends ShulkerEntity> entityType, World world) {
		super(entityType, world);
	}

	public CreeperfallShulkerEntity(World world) {
		this(EntityType.SHULKER, world);
	}

	protected void initGoals() {
	}
}
