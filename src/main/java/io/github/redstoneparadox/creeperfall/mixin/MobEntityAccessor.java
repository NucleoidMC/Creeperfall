package io.github.redstoneparadox.creeperfall.mixin;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEntity.class)
public interface MobEntityAccessor {
	@Accessor("experiencePoints")
	public void setExperiencePoints(int experiencePoints);
}
