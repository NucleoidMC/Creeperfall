package io.github.redstoneparadox.creeperfall.game.spawning;

import io.github.redstoneparadox.creeperfall.entity.CreeperfallCreeperEntity;
import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import io.github.redstoneparadox.creeperfall.game.util.Timer;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.Random;

public class CreeperfallCreeperSpawnLogic {
	private final GameSpace gameSpace;
	private final CreeperfallActive game;
	private final CreeperfallMap map;
	private final CreeperfallConfig config;
	private final EntityTracker tracker;
	private final int stages;
	private final Random random;
	private final Timer spawnTimer;
	private final Timer creeperIncreaseTimer;
	private int currentStage = 1;
	private boolean spawnedFirstWave = false;

	public CreeperfallCreeperSpawnLogic(GameSpace gameSpace, CreeperfallActive game, CreeperfallMap map, CreeperfallConfig config, EntityTracker tracker) {
		this.gameSpace = gameSpace;
		this.game = game;
		this.map = map;
		this.config = config;
		this.tracker = tracker;
		this.stages = config.creeperSpawnConfig.stages;
		int stageLength = config.creeperSpawnConfig.stageLengthSeconds * 20;
		int spawnDelay = config.creeperSpawnConfig.spawnDelaySeconds * 20;
		this.random = new Random();
		this.spawnTimer = Timer.createRepeating(spawnDelay, this::spawnCreepers);
		this.creeperIncreaseTimer = Timer.createRepeating(stageLength, () -> {
			if (currentStage < stages) {
				currentStage += 1;
			}
		});
	}

	public void tick() {
		if (!spawnedFirstWave) {
			spawnCreepers();
			spawnedFirstWave = true;
		}

		creeperIncreaseTimer.tick();
		spawnTimer.tick();
	}

	private void spawnCreepers() {
		int playersRemaining = 0;

		for (ServerPlayerEntity playerEntity : gameSpace.getPlayers()) {
			if (!playerEntity.isSpectator()) {
				playersRemaining += 1;
			}
		}

		int minCreepers = playersRemaining;
		int maxCreepers = MathHelper.floor(currentStage * config.creeperSpawnConfig.spawnCountIncrement * playersRemaining);

		int count = MathHelper.nextInt(random, minCreepers, maxCreepers);

		for (int i = 0; i < count; i++) {
			spawnCreeper();
		}
	}

	private void spawnCreeper() {
		ServerWorld world = gameSpace.getWorld();
		CreeperEntity entity = new CreeperfallCreeperEntity(world, config.creeperSpawnConfig.fallSpeedMultiplier);

		int size = config.mapConfig.size;
		int radius = size/2;
		int x = random.nextInt(size) - radius;
		int y = map.spawn.getY() + config.creeperSpawnConfig.spawnHeight;
		int z = random.nextInt(size) - radius;

		entity.setHealth(0.5f);
		game.spawnEntity(entity, x, y, z, SpawnReason.NATURAL);
	}
}
