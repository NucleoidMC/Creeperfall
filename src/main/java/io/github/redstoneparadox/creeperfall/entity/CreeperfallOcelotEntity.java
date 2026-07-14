package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Set;

public class CreeperfallOcelotEntity extends Ocelot {
	private EntityTracker tracker;
	private int timeToDespawn = 30 * 20;

	@Deprecated
	public CreeperfallOcelotEntity(Level level) {
		super(EntityTypes.OCELOT, level);
	}

	public CreeperfallOcelotEntity(EntityTracker tracker, Level level) {
		this(level);
		this.tracker = tracker;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.targetSelector.addGoal(1, new CreeperfallFollowTargetGoal<>(this, Creeper.class, 10, false, false, (livingEntity, world) -> livingEntity.onGround(), false));
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Creeper.class, 128.0F));
	}

	@Override
	public void setSpeed(float movementSpeed) {
		super.setSpeed(movementSpeed * 3);
	}

	@Override
	public void tick() {
		Set<Creeper> creepers = tracker.getAll(EntityTypes.CREEPER);

		for (Creeper creeper: creepers) {
			if (position().distanceTo(creeper.position()) <= 4 && creeper.onGround()) {
				creeper.kill((ServerLevel) this.level());
			}
		}

		timeToDespawn -= 1;

		if (timeToDespawn <= 0 && !this.isRemoved()) {
			remove(RemovalReason.DISCARDED);
		}

		super.tick();
	}
}
