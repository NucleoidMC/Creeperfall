package io.github.redstoneparadox.creeperfall.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

public class LookUpAtEntityGoal extends LookAtPlayerGoal {
	public LookUpAtEntityGoal(Mob mob, Class<? extends LivingEntity> targetType, float range) {
		super(mob, targetType, range, 1.0f);
	}

	public boolean canUse() {
		if (this.mob.getRandom().nextFloat() >= this.probability) {
			return false;
		} else {
			if (this.mob.getTarget() != null) {
				this.lookAt = this.mob.getTarget();
			}

			if (this.lookAtType == Player.class) {
				this.lookAt = ((ServerLevel) mob.level()).getNearestPlayer(lookAtContext, mob, mob.getX(), mob.getEyeY(), mob.getZ());
			} else {
				this.lookAt = ((ServerLevel) mob.level()).getNearestEntity(lookAtType, lookAtContext, mob, mob.getX(), mob.getEyeY(), mob.getZ(), mob.getBoundingBox().inflate(lookDistance, lookDistance, lookDistance));
			}

			return this.lookAt != null;
		}
	}
}
