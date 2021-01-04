package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class AlwaysFollowTargetGoal<T extends LivingEntity> extends FollowTargetGoal<T> {
	public AlwaysFollowTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
	}

	@Override
	public boolean canStart() {
		this.findClosestTarget();
		return this.targetEntity != null;
	}
}
