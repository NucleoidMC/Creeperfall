package io.github.redstoneparadox.creeperfall.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class CreeperfallMapConfig {
    public static final Codec<CreeperfallMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
    ).apply(instance, CreeperfallMapConfig::new));

    public final BlockState spawnBlock;

    public CreeperfallMapConfig(BlockState spawnBlock) {
        this.spawnBlock = spawnBlock;
    }
}
