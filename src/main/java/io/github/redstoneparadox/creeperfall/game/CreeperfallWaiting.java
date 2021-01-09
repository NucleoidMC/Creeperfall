package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMapGenerator;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallPlayerSpawnLogic;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;

public class CreeperfallWaiting {
    private final GameSpace gameSpace;
    private final CreeperfallMap map;
    private final CreeperfallConfig config;
    private final CreeperfallPlayerSpawnLogic spawnLogic;

    private CreeperfallWaiting(GameSpace gameSpace, CreeperfallMap map, CreeperfallConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new CreeperfallPlayerSpawnLogic(gameSpace, map, config);
    }

    public static GameOpenProcedure open(GameOpenContext<CreeperfallConfig> context) {
        CreeperfallConfig config = context.getConfig();
        CreeperfallMapGenerator generator = new CreeperfallMapGenerator(config.mapConfig);
        CreeperfallMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR)
                .setGameRule(GameRules.NATURAL_REGENERATION, false);

        return context.createOpenProcedure(worldConfig, game -> {
            CreeperfallWaiting waiting = new CreeperfallWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        CreeperfallActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE, true);
        this.spawnLogic.spawnPlayer(player);
    }
}
