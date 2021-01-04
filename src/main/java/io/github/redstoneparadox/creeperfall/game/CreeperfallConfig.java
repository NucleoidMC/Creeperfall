package io.github.redstoneparadox.creeperfall.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMapConfig;

public class CreeperfallConfig {
    public static final Codec<CreeperfallConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            CreeperfallMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs),
            Codec.INT.fieldOf("max_creepers_per_player").forGetter(config -> config.maxCreepersPerPlayer),
            Codec.INT.fieldOf("stage_length_secs").forGetter(config -> config.stageLengthSeconds),
            Codec.INT.fieldOf("creeper_spawn_delay_secs").forGetter(config -> config.creeperSpawnDelaySecs)
    ).apply(instance, CreeperfallConfig::new));

    public final PlayerConfig playerConfig;
    public final CreeperfallMapConfig mapConfig;
    public final int timeLimitSecs;
    public final int maxCreepersPerPlayer;
    public final int stageLengthSeconds;
    public final int creeperSpawnDelaySecs;

    public CreeperfallConfig(PlayerConfig players, CreeperfallMapConfig mapConfig, int timeLimitSecs, int maxCreepersPerPlayer, int stageLengthSeconds, int creeperSpawnDelaySecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
        this.maxCreepersPerPlayer = maxCreepersPerPlayer;
        this.stageLengthSeconds = stageLengthSeconds;
        this.creeperSpawnDelaySecs = creeperSpawnDelaySecs;
    }
}
