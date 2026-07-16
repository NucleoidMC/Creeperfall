package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMapGenerator;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallPlayerSpawnLogic;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CreeperfallWaiting {
    private final GameSpace gameSpace;
    private final CreeperfallMap map;
    private final CreeperfallConfig config;
    private final CreeperfallPlayerSpawnLogic spawnLogic;
    private final ServerLevel level;

    private CreeperfallWaiting(GameSpace gameSpace, ServerLevel level, CreeperfallMap map, CreeperfallConfig config) {
        this.gameSpace = gameSpace;
        this.level = level;
        this.map = map;
        this.config = config;
        this.spawnLogic = new CreeperfallPlayerSpawnLogic(level, map);
    }

    public static GameOpenProcedure open(GameOpenContext<CreeperfallConfig> context) {
        CreeperfallConfig config = context.config();
        CreeperfallMapGenerator generator = new CreeperfallMapGenerator(config.mapConfig);
        CreeperfallMap map = generator.build();

        RuntimeLevelConfig levelConfig = new RuntimeLevelConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);

        return context.openWithLevel(levelConfig, (game, level) -> {
            CreeperfallWaiting waiting = new CreeperfallWaiting(game.getGameSpace(), level, map, context.config());

            GameWaitingLobby.addTo(game, config.playerConfig);
            game.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(level, Vec3.atBottomCenterOf(map.spawn)));
            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
            game.listen(ItemUseEvent.EVENT, waiting::onItemUse);
        });
    }

    private GameResult requestStart() {
        CreeperfallActive.open(this.gameSpace, this.level, this.map, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayer player) {
        this.spawnPlayer(player);
    }

    private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return EventResult.DENY;
    }

    private void spawnPlayer(ServerPlayer player) {
        this.spawnLogic.resetPlayer(player, this.gameSpace.getPlayers().participants().contains(player) ? GameType.ADVENTURE : GameType.SPECTATOR, true);
        this.spawnLogic.spawnPlayer(player);
    }

    private InteractionResult onItemUse(ServerPlayer player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.is(Items.WRITTEN_BOOK)) {
            //player.currentScreenHandler.sendContentUpdates();
            player.connection.send(new ClientboundOpenBookPacket(hand));
        }

        return InteractionResult.SUCCESS_SERVER;
    }
}
