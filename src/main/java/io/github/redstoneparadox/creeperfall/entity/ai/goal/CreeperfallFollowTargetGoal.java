package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CreeperfallFollowTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
	private final boolean airborneTargetsOnly;

	public CreeperfallFollowTargetGoal(Mob mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable TargetingConditions.Selector targetPredicate) {
		super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
		this.airborneTargetsOnly = true;
	}

	public CreeperfallFollowTargetGoal(Mob mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable TargetingConditions.Selector targetPredicate, boolean airborneTargetsOnly) {
		super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
		this.airborneTargetsOnly = airborneTargetsOnly;
	}

	@Override
	protected AABB getTargetSearchArea(double distance) {
		return this.mob.getBoundingBox().inflate(distance, distance, distance);
	}

	@Override
	protected double getFollowDistance() {
		return super.getFollowDistance() * 16.0;
	}

	@Override
	public boolean canUse() {
		this.findTarget();
		return this.target != null && canTarget();
	}

	@Override
	public boolean canContinueToUse() {
		return super.canContinueToUse() && canTarget();
	}

	private boolean canTarget() {
		return !target.onGround() || !airborneTargetsOnly;
	}
}
