package io.github.redstoneparadox.creeperfall.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class CreeperfallMap {
    private final MapTemplate template;
    private final CreeperfallMapConfig config;
    public BlockPos spawn;

    public CreeperfallMap(MapTemplate template, CreeperfallMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
