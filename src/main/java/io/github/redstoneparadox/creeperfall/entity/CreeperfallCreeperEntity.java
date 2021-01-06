package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Vec3d;
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
		}

		if (!isInvulnerable()) {
			Vec3d velocity = getVelocity();
			setVelocity(velocity.multiply(1.0, 0.75, 1.0));
		}

		if (getY() <= 0) {
			kill();
		}
		super.tick();
	}
}
