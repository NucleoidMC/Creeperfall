package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class LookUpAtEntityGoal extends LookAtEntityGoal {
	public LookUpAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range) {
		super(mob, targetType, range, 1.0f);
	}

	public boolean canStart() {
		if (this.mob.getRandom().nextFloat() >= this.chance) {
			return false;
		} else {
			if (this.mob.method_5968() != null) {
				this.target = this.mob.method_5968();
			}

			if (this.targetType == PlayerEntity.class) {
				this.target = mob.world.getClosestPlayer(targetPredicate, mob, mob.getX(), mob.getEyeY(), mob.getZ());
			} else {
				this.target = mob.world.getClosestEntity(targetType, targetPredicate, mob, mob.getX(), mob.getEyeY(), mob.getZ(), mob.getBoundingBox().expand(range, range, range));
			}

			return this.target != null;
		}
	}
}
