package io.github.redstoneparadox.creeperfall.mixin;

import io.github.redstoneparadox.creeperfall.hooks.CreeperHooks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity implements CreeperHooks {
	@Shadow private int explosionRadius;
	@Unique
	private boolean isCreeperfallCreeper = false;

	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		if (isCreeperfallCreeper) {
			if (isOnGround()) {
				setInvulnerable(true);
				removeStatusEffect(StatusEffects.SLOW_FALLING);
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
