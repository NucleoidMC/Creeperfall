package io.github.redstoneparadox.creeperfall.game.spawning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CreeperfallCreeperSpawnConfig {
	public static final Codec<CreeperfallCreeperSpawnConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("max_creepers_per_player").forGetter(config -> config.maxCreepersPerPlayer),
			Codec.INT.fieldOf("stage_length_secs").forGetter(config -> config.stageLengthSeconds),
			Codec.INT.fieldOf("creeper_spawn_delay_secs").forGetter(config -> config.creeperSpawnDelaySeconds),
			Codec.INT.fieldOf("creeper_spawn_height").forGetter(config -> config.creeperSpawnHeight),
			Codec.doubleRange(0.0, 1.0).fieldOf("creeper_fall_speed_multiplier").forGetter(config -> config.creeperFallSpeedMultiplier)
	).apply(instance, CreeperfallCreeperSpawnConfig::new));

	public final int maxCreepersPerPlayer;
	public final int stageLengthSeconds;
	public final int creeperSpawnDelaySeconds;
	public final int creeperSpawnHeight;
	public final double creeperFallSpeedMultiplier;

	public CreeperfallCreeperSpawnConfig(int maxCreepersPerPlayer, int stageLengthSeconds, int creeperSpawnDelaySeconds, int creeperSpawnHeight, double creeperFallSpeedMultiplier) {
		this.maxCreepersPerPlayer = maxCreepersPerPlayer;
		this.stageLengthSeconds = stageLengthSeconds;
		this.creeperSpawnDelaySeconds = creeperSpawnDelaySeconds;
		this.creeperSpawnHeight = creeperSpawnHeight;
		this.creeperFallSpeedMultiplier = creeperFallSpeedMultiplier;
	}
}
