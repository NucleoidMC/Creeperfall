package io.github.redstoneparadox.creeperfall.game.spawning;

import io.github.redstoneparadox.creeperfall.entity.CreeperfallCreeperEntity;
import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import io.github.redstoneparadox.creeperfall.game.util.Timer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import xyz.nucleoid.plasmid.api.game.GameSpace;

public class CreeperfallCreeperSpawnLogic {
	private final GameSpace gameSpace;
	private final CreeperfallActive game;
	private final CreeperfallMap map;
	private final CreeperfallConfig config;
	private final EntityTracker tracker;
	private final int stages;
	private final RandomSource random;
	private final Timer spawnTimer;
	private final Timer creeperIncreaseTimer;
	private final ServerLevel level;
	private int currentStage = 1;
	private boolean spawnedFirstWave = false;

	public CreeperfallCreeperSpawnLogic(GameSpace gameSpace, ServerLevel level, CreeperfallActive game, CreeperfallMap map, CreeperfallConfig config, EntityTracker tracker) {
		this.gameSpace = gameSpace;
		this.level = level;
		this.game = game;
		this.map = map;
		this.config = config;
		this.tracker = tracker;
		this.stages = config.creeperConfig.stages;
		int stageLength = config.creeperConfig.stageLengthSeconds * 20;
		int spawnDelay = config.creeperConfig.spawnDelaySeconds * 20;
		this.random = RandomSource.create();
		this.spawnTimer = Timer.createRepeating(spawnDelay, this::spawnCreepers);
		this.creeperIncreaseTimer = Timer.createRepeating(stageLength, () -> {
			if (currentStage < stages) {
				currentStage += 1;
				game.announceStage(currentStage);
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

		for (ServerPlayer playerEntity : gameSpace.getPlayers()) {
			if (!playerEntity.isSpectator()) {
				playersRemaining += 1;
			}
		}

		int minCreepers = playersRemaining;
		int maxCreepers = Mth.floor(currentStage * config.creeperConfig.spawnCountIncrement * playersRemaining);

		int count = Mth.nextInt(random, minCreepers, maxCreepers);

		for (int i = 0; i < count; i++) {
			spawnCreeper();
		}
	}

	private void spawnCreeper() {
		int size = config.mapConfig.size;
		double radius = size / 2.0 - 0.5;
		int adjustmentConst = 0;

		if (size % 2 == 1) adjustmentConst = 1;

		double negativeBound = -radius - adjustmentConst;

		double x = random.nextInt(size - 2) + negativeBound + 1;
		int y = map.spawn.getY() + config.creeperConfig.spawnHeight;
		double z = random.nextInt(size - 2) + negativeBound + 1;

		Creeper entity = new CreeperfallCreeperEntity(this.level, config.creeperConfig.fallSpeedMultiplier, 0.02, 0.02);

		entity.setHealth(0.5f);
		game.spawnEntity(entity, x, y, z, EntitySpawnReason.NATURAL);
	}

}
