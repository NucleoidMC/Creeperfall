package io.github.redstoneparadox.creeperfall.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMapConfig;
import io.github.redstoneparadox.creeperfall.game.shop.CreeperfallShopConfig;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallCreeperSpawnConfig;
import io.github.redstoneparadox.creeperfall.game.util.Codecs;
import net.minecraft.util.math.IntRange;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class CreeperfallConfig {
    public static final Codec<CreeperfallConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            CreeperfallMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            CreeperfallShopConfig.CODEC.fieldOf("shop").forGetter(config -> config.shopConfig),
            CreeperfallCreeperSpawnConfig.CODEC.fieldOf("creeper_spawning").forGetter(config -> config.creeperSpawnConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs),
            Codec.INT.fieldOf("max_arrows").forGetter(config -> config.maxArrows),
            Codec.INT.fieldOf("arrow_replenish_time_secs").forGetter(config -> config.arrowReplenishTimeSeconds),
            Codecs.INT_RANGE.fieldOf("emerald_reward_count").forGetter(config -> config.emeraldRewardCount)
    ).apply(instance, CreeperfallConfig::new));

    public final PlayerConfig playerConfig;
    public final CreeperfallMapConfig mapConfig;
    public final CreeperfallShopConfig shopConfig;
    public final CreeperfallCreeperSpawnConfig creeperSpawnConfig;
    public final int timeLimitSecs;
    public final int maxArrows;
    public final int arrowReplenishTimeSeconds;
    public final IntRange emeraldRewardCount;

    public CreeperfallConfig(
            PlayerConfig playerConfig,
            CreeperfallMapConfig mapConfig,
            CreeperfallShopConfig shopConfig,
            CreeperfallCreeperSpawnConfig creeperSpawnConfig,
            int timeLimitSecs,
            int maxArrows,
            int arrowReplenishTimeSeconds,
            IntRange emeraldRewardCount
    ) {
        this.playerConfig = playerConfig;
        this.mapConfig = mapConfig;
        this.shopConfig = shopConfig;
        this.creeperSpawnConfig = creeperSpawnConfig;
        this.timeLimitSecs = timeLimitSecs;
        this.maxArrows = maxArrows;
        this.arrowReplenishTimeSeconds = arrowReplenishTimeSeconds;
        this.emeraldRewardCount = emeraldRewardCount;
    }
}
