package io.github.redstoneparadox.creeperfall.mixin;

import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSkeletonEntity.class)
public interface AbstractSkeletonEntityAccessor {
	@Accessor("bowAttackGoal")
	BowAttackGoal<AbstractSkeletonEntity> getBowAttackGoal();

	@Accessor("bowAttackGoal")
	@Mutable
	void setBowAttackGoal(BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal);
}
