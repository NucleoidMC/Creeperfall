package io.github.redstoneparadox.creeperfall.mixin;

import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSkeleton.class)
public interface AbstractSkeletonAccessor {
	@Accessor("bowGoal")
	RangedBowAttackGoal<AbstractSkeleton> getBowGoal();

	@Accessor("bowGoal")
	@Mutable
	void setBowGoal(RangedBowAttackGoal<AbstractSkeleton> bowAttackGoal);
}
