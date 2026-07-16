package io.github.redstoneparadox.creeperfall.mixin;

import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Guardian.class)
public interface GuardianAccessor {
	@Invoker("setActiveAttackTarget")
	void invokeSetActiveAttackTarget(int i);
}
