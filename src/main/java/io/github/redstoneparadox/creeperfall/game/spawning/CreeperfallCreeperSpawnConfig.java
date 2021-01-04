package io.github.redstoneparadox.creeperfall.game.spawning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.redstoneparadox.creeperfall.game.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMapConfig;
import io.github.redstoneparadox.creeperfall.game.shop.CreeperfallShopConfig;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CreeperfallCreeperSpawnConfig {
	public static final Codec<CreeperfallCreeperSpawnConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("max_creepers_per_player").forGetter(config -> config.maxCreepersPerPlayer),
			Codec.INT.fieldOf("stage_length_secs").forGetter(config -> config.stageLengthSeconds),
			Codec.INT.fieldOf("creeper_spawn_delay_secs").forGetter(config -> config.creeperSpawnDelaySeconds),
			Codec.INT.fieldOf("creeper_spawn_height").forGetter(config -> config.creeperSpawnHeight)
	).apply(instance, CreeperfallCreeperSpawnConfig::new));

	public final int maxCreepersPerPlayer;
	public final int stageLengthSeconds;
	public final int creeperSpawnDelaySeconds;
	public final int creeperSpawnHeight;

	public CreeperfallCreeperSpawnConfig(int maxCreepersPerPlayer, int stageLengthSeconds, int creeperSpawnDelaySeconds, int creeperSpawnHeight) {
		this.maxCreepersPerPlayer = maxCreepersPerPlayer;
		this.stageLengthSeconds = stageLengthSeconds;
		this.creeperSpawnDelaySeconds = creeperSpawnDelaySeconds;
		this.creeperSpawnHeight = creeperSpawnHeight;
	}
}
