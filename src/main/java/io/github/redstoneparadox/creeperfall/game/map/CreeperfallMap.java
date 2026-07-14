package io.github.redstoneparadox.creeperfall.game.map;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallMapConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

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
