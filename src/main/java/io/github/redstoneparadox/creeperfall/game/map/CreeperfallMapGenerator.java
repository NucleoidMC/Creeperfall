package io.github.redstoneparadox.creeperfall.game.map;

import net.minecraft.block.Blocks;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.WallShape;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class CreeperfallMapGenerator {

    private final CreeperfallMapConfig config;

    public CreeperfallMapGenerator(CreeperfallMapConfig config) {
        this.config = config;
    }

    public CreeperfallMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        CreeperfallMap map = new CreeperfallMap(template, this.config);

        this.buildMainPlatform(template);
        this.buildPerimeter(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

    private void buildMainPlatform(MapTemplate builder) {
        int radius = config.size/2;
        BlockPos min = new BlockPos(-radius, 64, -radius);
        BlockPos max = new BlockPos(radius, 64, radius);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            if (pos.getX() == 0 || pos.getZ() == 0) {
                builder.setBlockState(pos, Blocks.RED_STAINED_GLASS.getDefaultState());
            }
            else {
                builder.setBlockState(pos, this.config.spawnBlock);
            }
        }
    }

    private void buildPerimeter(MapTemplate builder) {
        int radius = config.size/2 + 1;

        BlockPos min = new BlockPos(-radius, 64, -radius);
        BlockPos max = new BlockPos(radius, 64, radius);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            if (builder.getBlockState(pos).isAir()) {
                builder.setBlockState(pos, Blocks.BLACK_TERRACOTTA.getDefaultState());

                if (Math.abs(pos.getX()) == Math.abs(pos.getZ())) {
                    builder.setBlockState(pos.up(), Blocks.BRICKS.getDefaultState());
                    builder.setBlockState(pos.up(2), Blocks.BRICK_SLAB.getDefaultState());
                } else if (pos.getX() == radius || pos.getX() == -radius) {
                    builder.setBlockState(
                            pos.up(),
                            Blocks.BRICK_WALL
                                    .getDefaultState()
                                    .with(WallBlock.NORTH_SHAPE, WallShape.LOW)
                                    .with(WallBlock.SOUTH_SHAPE, WallShape.LOW)
                                    .with(WallBlock.UP, false)
                    );
                } else {
                    builder.setBlockState(
                            pos.up(),
                            Blocks.BRICK_WALL
                                    .getDefaultState()
                                    .with(WallBlock.EAST_SHAPE, WallShape.LOW)
                                    .with(WallBlock.WEST_SHAPE, WallShape.LOW)
                                    .with(WallBlock.UP, false)
                    );
                }
            }
        }
    }
}
