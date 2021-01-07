package io.github.redstoneparadox.creeperfall.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CreeperfallCreeperEntity extends CreeperEntity {
	private double fallSpeedMultiplier;
	private int ticksUntilAutoIgnite = 3 * 20;

	public CreeperfallCreeperEntity(World world) {
		super(EntityType.CREEPER, world);
	}

	public CreeperfallCreeperEntity(World world, double fallSpeedMultiplier) {
		this(world);
		this.fallSpeedMultiplier = fallSpeedMultiplier;
		this.experiencePoints = 0;
	}

	@Override
	public void tick() {
		if (isOnGround()) {
			setInvulnerable(true);

			if (ticksUntilAutoIgnite > 0 && !getIgnited()) {
				ticksUntilAutoIgnite -= 1;
			}
			else {
				ignite();
			}
		}

		if (!isInvulnerable()) {
			Vec3d velocity = getVelocity();
			setVelocity(velocity.multiply(1.0, fallSpeedMultiplier, 1.0));
		}

		if (getY() <= 0) {
			kill();
		}
		super.tick();
	}

	public boolean handleFallDamage(float f, float g) {
		return false;
	}
}
