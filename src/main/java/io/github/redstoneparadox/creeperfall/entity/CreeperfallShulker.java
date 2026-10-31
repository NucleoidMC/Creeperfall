package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;

public class CreeperfallShulker extends Shulker {
	public CreeperfallShulker(EntityType<? extends Shulker> entityType, Level world) {
		super(entityType, world);
	}

	public CreeperfallShulker(Level level) {
		this(EntityTypes.SHULKER, level);
	}

	protected void registerGoals() {
	}
}
