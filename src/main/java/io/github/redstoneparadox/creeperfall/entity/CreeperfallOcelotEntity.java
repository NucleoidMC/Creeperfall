package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

public class CreeperfallOcelotEntity extends OcelotEntity {
	private EntityTracker tracker;
	private int timeToDespawn = 3 * 20;

	@Deprecated
	public CreeperfallOcelotEntity(World world) {
		super(EntityType.OCELOT, world);
	}

	public CreeperfallOcelotEntity(EntityTracker tracker, World world) {
		this(world);
		this.tracker = tracker;
	}

	@Override
	public void setMovementSpeed(float movementSpeed) {

	}

	@Override
	public void setVelocity(Vec3d velocity) {

	}

	@Override
	public void tick() {
		timeToDespawn -= 1;

		if (timeToDespawn <= 0 && !removed) {
			Set<CreeperEntity> creepers = tracker.getAll(EntityType.CREEPER);

			for (CreeperEntity creeperEntity: creepers) {
				creeperEntity.kill();
			}

			remove();
		}

		super.tick();
	}
}
