package io.github.redstoneparadox.creeperfall.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CreeperfallCreeperSpawnConfig {
	public static final Codec<CreeperfallCreeperSpawnConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("stages").forGetter(config -> config.stages),
			Codec.INT.fieldOf("stage_length_secs").forGetter(config -> config.stageLengthSeconds),
			Codec.DOUBLE.fieldOf("spawn_count_increment").forGetter(config -> config.spawnCountIncrement),
			Codec.INT.fieldOf("spawn_delay_secs").forGetter(config -> config.spawnDelaySeconds),
			Codec.INT.fieldOf("spawn_height").forGetter(config -> config.spawnHeight),
			Codec.doubleRange(0.0, 1.0).fieldOf("fall_speed_multiplier").forGetter(config -> config.fallSpeedMultiplier)
	).apply(instance, CreeperfallCreeperSpawnConfig::new));

	public final int stages;
	public final int stageLengthSeconds;
	public final double spawnCountIncrement;
	public final int spawnDelaySeconds;
	public final int spawnHeight;
	public final double fallSpeedMultiplier;

	public CreeperfallCreeperSpawnConfig(int stages, int stageLengthSeconds, double spawnCountIncrement, int spawnDelaySeconds, int spawnHeight, double fallSpeedMultiplier) {
		this.stages = stages;
		this.stageLengthSeconds = stageLengthSeconds;
		this.spawnCountIncrement = spawnCountIncrement;
		this.spawnDelaySeconds = spawnDelaySeconds;
		this.spawnHeight = spawnHeight;
		this.fallSpeedMultiplier = fallSpeedMultiplier;
	}
}
