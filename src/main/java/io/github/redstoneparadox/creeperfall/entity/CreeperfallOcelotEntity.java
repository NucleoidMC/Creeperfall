package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.world.World;

import java.util.Set;

public class CreeperfallOcelotEntity extends OcelotEntity {
	EntityTracker tracker;

	@Deprecated
	public CreeperfallOcelotEntity(World world) {
		super(EntityType.OCELOT, world);
	}

	public CreeperfallOcelotEntity(EntityTracker tracker, World world) {
		this(world);
		this.tracker = tracker;
	}

	@Override
	public void tick() {
		Set<CreeperEntity> creepers = tracker.getAll(EntityType.CREEPER);

		for (CreeperEntity creeperEntity: creepers) {
			creeperEntity.kill();
		}

		super.tick();
	}
}
