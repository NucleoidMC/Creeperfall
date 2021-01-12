package io.github.redstoneparadox.creeperfall.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class CreeperfallMapConfig {
    public static final Codec<CreeperfallMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock),
            Codec.INT.fieldOf("size").forGetter(map -> map.size)
    ).apply(instance, CreeperfallMapConfig::new));

    public final BlockState spawnBlock;
    public final int size;

    public CreeperfallMapConfig(BlockState spawnBlock, int size) {
        this.spawnBlock = spawnBlock;
        this.size = size;
    }
}
