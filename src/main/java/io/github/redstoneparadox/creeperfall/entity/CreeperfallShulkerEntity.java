package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CreeperfallShulkerEntity extends Shulker {
	public CreeperfallShulkerEntity(EntityType<? extends Shulker> entityType, Level world) {
		super(entityType, world);
	}

	public CreeperfallShulkerEntity(Level level) {
		this(EntityTypes.SHULKER, level);
	}

	protected void registerGoals() {
	}
}
