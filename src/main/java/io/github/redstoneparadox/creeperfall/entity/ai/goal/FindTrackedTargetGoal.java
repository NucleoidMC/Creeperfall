package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FindTrackedTargetGoal<T extends LivingEntity> extends TargetGoal {
	private final EntityType<T> trackedType;
	private final Supplier<EntityTracker> tracker;
	private final Predicate<T> filter;
	private @Nullable T target = null;

	public FindTrackedTargetGoal(Mob mob, EntityType<T> trackedType, Supplier<EntityTracker> tracker, Predicate<T> filter) {
		super(mob, false);
		this.trackedType = trackedType;
		this.tracker = tracker;
		this.filter = filter;
	}

	@Override
	public boolean canUse() {
		findTarget();
		return target != null;
	}

	@Override
	public void start() {
		this.mob.setTarget(target);
		super.start();
	}

	@Override
	public boolean canContinueToUse() {
		if (target == null || target.isRemoved()) {
			return false;
		}

		return !filter.test(target);
	}

	private void findTarget() {
		Set<T> entities = tracker.get().getAll(trackedType);
		entities.removeIf(filter);

		for (T entity: entities) {
			if (target == null || entity.distanceTo(mob) < target.distanceTo(mob)) {
				target = entity;
			}
		}
	}
}
