package io.github.redstoneparadox.creeperfall.mixin;

import io.github.redstoneparadox.creeperfall.hooks.CreeperHooks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity implements CreeperHooks {
	private boolean isCreeperfallCreeper = false;

	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		if (isCreeperfallCreeper) {
			if (isOnGround()) {
				setInvulnerable(true);
			}
			if (getY() <= 0) {
				kill();
			}
		}
	}

	@Override
	public void setCreeperfallCreeper(boolean isCreeperfallCreeper) {
		this.isCreeperfallCreeper = isCreeperfallCreeper;
	}

	@Override
	public boolean isCreeperfallCreeper() {
		return isCreeperfallCreeper;
	}
}
