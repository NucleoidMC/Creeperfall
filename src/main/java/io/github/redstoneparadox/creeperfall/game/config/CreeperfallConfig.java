package io.github.redstoneparadox.creeperfall.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.redstoneparadox.creeperfall.game.util.Codecs;
import net.minecraft.predicate.NumberRange;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

import java.util.List;

public class CreeperfallConfig {
    public static final MapCodec<CreeperfallConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            CreeperfallMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            CreeperfallShopConfig.CODEC.fieldOf("shop").forGetter(config -> config.shopConfig),
            CreeperfallCreeperConfig.CODEC.fieldOf("creepers").forGetter(config -> config.creeperConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs),
            Codec.INT.listOf().fieldOf("max_arrows").forGetter(config -> config.maxArrows),
            Codec.INT.fieldOf("arrow_replenish_time_secs").forGetter(config -> config.arrowReplenishTimeSeconds),
            Codecs.INT_RANGE.fieldOf("emerald_reward_count").forGetter(config -> config.emeraldRewardCount)
    ).apply(instance, CreeperfallConfig::new));

    public final WaitingLobbyConfig playerConfig;
    public final CreeperfallMapConfig mapConfig;
    public final CreeperfallShopConfig shopConfig;
    public final CreeperfallCreeperConfig creeperConfig;
    public final int timeLimitSecs;
    public final List<Integer> maxArrows;
    public final int arrowReplenishTimeSeconds;
    public final NumberRange.IntRange emeraldRewardCount;

    public CreeperfallConfig(
            WaitingLobbyConfig playerConfig,
            CreeperfallMapConfig mapConfig,
            CreeperfallShopConfig shopConfig,
            CreeperfallCreeperConfig creeperConfig,
            int timeLimitSecs,
            List<Integer>  maxArrows,
            int arrowReplenishTimeSeconds,
            NumberRange.IntRange emeraldRewardCount
    ) {
        this.playerConfig = playerConfig;
        this.mapConfig = mapConfig;
        this.shopConfig = shopConfig;
        this.creeperConfig = creeperConfig;
        this.timeLimitSecs = timeLimitSecs;
        this.maxArrows = maxArrows;
        this.arrowReplenishTimeSeconds = arrowReplenishTimeSeconds;
        this.emeraldRewardCount = emeraldRewardCount;
    }
}
