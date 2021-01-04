package io.github.redstoneparadox.creeperfall.game.map;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;

public class CreeperfallMapGenerator {

    private final CreeperfallMapConfig config;

    public CreeperfallMapGenerator(CreeperfallMapConfig config) {
        this.config = config;
    }

    public CreeperfallMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        CreeperfallMap map = new CreeperfallMap(template, this.config);

        this.buildSpawn(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

    private void buildSpawn(MapTemplate builder) {
        int radius = config.size/2;
        BlockPos min = new BlockPos(-radius, 64, -radius);
        BlockPos max = new BlockPos(radius, 64, radius);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            builder.setBlockState(pos, this.config.spawnBlock);
        }
    }
}
