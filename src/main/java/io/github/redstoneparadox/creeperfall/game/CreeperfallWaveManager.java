package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.entity.CreeperfallCreeper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Creeper;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameSpace;

public final class CreeperfallWaveManager {
	private final GameSpace gameSpace;
	private final CreeperfallActive game;
	private final ServerLevel level;
	private int totalCreepers = 10;
	private int creepersRemaining = 0;
	private int unspawnedCreepers = totalCreepers;
	private int livingCreepers = 0;
	private final int totalWaves;
	private int currentWave = 1;
	private int countdownToNextSpawn = 0;
	private final BlockBounds spawningBounds;
	private final RandomSource random;

	public CreeperfallWaveManager(GameSpace gameSpace, CreeperfallActive game, ServerLevel level, int totalWaves, BlockBounds spawningBounds, RandomSource random) {
		this.gameSpace = gameSpace;
		this.game = game;
		this.level = level;
		this.totalWaves = totalWaves;
		this.spawningBounds = spawningBounds;
		this.random = random;

		ServerLivingEntityEvents.AFTER_DEATH.register(((entity, damageSource) -> {
			if (entity instanceof CreeperfallCreeper) {
				if (livingCreepers > 0) {
					livingCreepers -= 1;
					creepersRemaining -= 1;
				} else {
					// TODO: Log because this shouldn't happen.
				}
			}
		}));
	}

	public boolean tick() {
		// All creepers have been spawned and killed
		if (unspawnedCreepers > 0) countdownToNextSpawn -= 1;

		if (creepersRemaining == 0 && livingCreepers == 0) {
			if (currentWave >= totalWaves) return true;

			this.totalCreepers += 10;
			this.creepersRemaining = totalCreepers;
			this.unspawnedCreepers = totalCreepers;
			countdownToNextSpawn = 20;
		} else if (unspawnedCreepers > 0) {
			spawnCreeper();
			unspawnedCreepers -= 1;
			countdownToNextSpawn = 20;
		}

		return false;
	}

	public void updateWaveBar(CreeperfallWaveBar waveBar) {
		waveBar.update(currentWave, totalWaves, creepersRemaining, totalCreepers);
	}

	private void spawnCreeper() {
		BlockPos min = this.spawningBounds.min();
		BlockPos max = this.spawningBounds.max();

		double x = random.nextIntBetweenInclusive(min.getX(), max.getX());
		double y = random.nextIntBetweenInclusive(min.getY(), max.getY());
		double z = random.nextIntBetweenInclusive(min.getZ(), max.getZ());

		Creeper creeper = new CreeperfallCreeper(this.level, 0.75, 0.02, 0.02);

		creeper.setHealth(0.5f);
		game.spawnEntity(creeper, x, y, z, EntitySpawnReason.MOB_SUMMONED);
	}
}
