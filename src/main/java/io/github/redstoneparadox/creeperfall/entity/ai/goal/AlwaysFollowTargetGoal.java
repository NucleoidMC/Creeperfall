package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class AlwaysFollowTargetGoal<T extends LivingEntity> extends FollowTargetGoal<T> {
	public AlwaysFollowTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
	}

	@Override
	protected Box getSearchBox(double distance) {
		return this.mob.getBoundingBox().expand(distance, distance, distance);
	}

	@Override
	protected double getFollowRange() {
		return super.getFollowRange() * 16.0;
	}

	@Override
	public boolean canStart() {
		this.findClosestTarget();
		return this.targetEntity != null;
	}
}
