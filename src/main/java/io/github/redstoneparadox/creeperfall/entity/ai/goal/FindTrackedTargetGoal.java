package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FindTrackedTargetGoal<T extends LivingEntity> extends TrackTargetGoal {
	private final EntityType<T> trackedType;
	private final Supplier<EntityTracker> tracker;
	private final Predicate<T> filter;
	private @Nullable T target = null;

	public FindTrackedTargetGoal(MobEntity mob, EntityType<T> trackedType, Supplier<EntityTracker> tracker, Predicate<T> filter) {
		super(mob, false);
		this.trackedType = trackedType;
		this.tracker = tracker;
		this.filter = filter;
	}

	@Override
	public boolean canStart() {
		findTarget();
		return target != null;
	}

	@Override
	public void start() {
		this.mob.setTarget(target);
		super.start();
	}

	@Override
	public boolean shouldContinue() {
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
