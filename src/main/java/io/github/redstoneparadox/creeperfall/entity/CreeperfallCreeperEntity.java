package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;

public class CreeperfallCreeperEntity extends CreeperEntity {
	public CreeperfallCreeperEntity(World world) {
		super(EntityType.CREEPER, world);
		this.experiencePoints = 0;
	}

	@Override
	public void tick() {
		if (isOnGround()) {
			setInvulnerable(true);
			removeStatusEffect(StatusEffects.SLOW_FALLING);
		}
		if (getY() <= 0) {
			kill();
		}
		super.tick();
	}
}
