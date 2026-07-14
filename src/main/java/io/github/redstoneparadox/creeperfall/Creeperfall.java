package io.github.redstoneparadox.creeperfall;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.CreeperfallWaiting;
import io.github.redstoneparadox.creeperfall.item.CreeperfallItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;

public class Creeperfall implements ModInitializer {

    public static final String ID = "creeperfall";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<CreeperfallConfig> TYPE = GameTypes.register(
            Identifier.fromNamespaceAndPath(ID, "creeperfall"),
            CreeperfallConfig.CODEC,
            CreeperfallWaiting::open
    );

    @Override
    public void onInitialize() {
        CreeperfallItems.init();
    }
}
