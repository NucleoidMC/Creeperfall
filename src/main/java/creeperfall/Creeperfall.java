package creeperfall;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import creeperfall.game.CreeperfallConfig;
import creeperfall.game.CreeperfallWaiting;

public class Creeperfall implements ModInitializer {

    public static final String ID = "creeperfall";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<CreeperfallConfig> TYPE = GameType.register(
            new Identifier(ID, "creeperfall"),
            CreeperfallWaiting::open,
            CreeperfallConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
