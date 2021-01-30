package io.github.redstoneparadox.creeperfall.entity;

import io.github.redstoneparadox.creeperfall.entity.ai.goal.CreeperfallFollowTargetGoal;
import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

public class CreeperfallOcelotEntity extends OcelotEntity {
	private EntityTracker tracker;
	private int timeToDespawn = 15 * 20;

	@Deprecated
	public CreeperfallOcelotEntity(World world) {
		super(EntityType.OCELOT, world);
	}

	public CreeperfallOcelotEntity(EntityTracker tracker, World world) {
		this(world);
		this.tracker = tracker;
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.targetSelector.add(1, new CreeperfallFollowTargetGoal<>(this, CreeperEntity.class, 10, false, false, Entity::isOnGround));
		this.goalSelector.add(1, new LookAtEntityGoal(this, CreeperEntity.class, 128.0F));
	}

	@Override
	public void tick() {
		Set<CreeperEntity> creepers = tracker.getAll(EntityType.CREEPER);

		for (CreeperEntity creeper: creepers) {
			if (getPos().distanceTo(creeper.getPos()) <= 4 && creeper.isOnGround()) {
				creeper.kill();
			}
		}

		timeToDespawn -= 1;

		if (timeToDespawn <= 0 && !removed) {
			remove();
		}

		super.tick();
	}
}
