package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CreeperfallFollowTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
	private final boolean airborneTargetsOnly;

	public CreeperfallFollowTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
		this.airborneTargetsOnly = true;
	}

	public CreeperfallFollowTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate, boolean airborneTargetsOnly) {
		super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
		this.airborneTargetsOnly = airborneTargetsOnly;
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
		return this.targetEntity != null && canTarget();
	}

	@Override
	public boolean shouldContinue() {
		return super.shouldContinue() && canTarget();
	}

	private boolean canTarget() {
		return !targetEntity.isOnGround() || !airborneTargetsOnly;
	}
}
