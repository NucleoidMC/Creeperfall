package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMapGenerator;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallPlayerSpawnLogic;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CreeperfallWaiting {
    private final GameSpace gameSpace;
    private final CreeperfallMap map;
    private final CreeperfallConfig config;
    private final CreeperfallPlayerSpawnLogic spawnLogic;
    private final ServerWorld world;

    private CreeperfallWaiting(GameSpace gameSpace, ServerWorld world, CreeperfallMap map, CreeperfallConfig config) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.map = map;
        this.config = config;
        this.spawnLogic = new CreeperfallPlayerSpawnLogic(world, map);
    }

    public static GameOpenProcedure open(GameOpenContext<CreeperfallConfig> context) {
        CreeperfallConfig config = context.config();
        CreeperfallMapGenerator generator = new CreeperfallMapGenerator(config.mapConfig);
        CreeperfallMap map = generator.build();

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setGameRule(GameRules.NATURAL_REGENERATION, false);

        return context.openWithWorld(worldConfig, (game, world) -> {
            CreeperfallWaiting waiting = new CreeperfallWaiting(game.getGameSpace(), world, map, context.config());

            GameWaitingLobby.addTo(game, config.playerConfig);
            game.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, Vec3d.ZERO));
            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
            game.listen(ItemUseEvent.EVENT, waiting::onItemUse);
        });
    }

    private GameResult requestStart() {
        CreeperfallActive.open(this.gameSpace, this.world, this.map, this.config);
        return GameResult.ok();
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

    private TypedActionResult<ItemStack> onItemUse(ServerPlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (stack.isOf(Items.WRITTEN_BOOK)) {
            if (WrittenBookItem.resolve(stack, player.getCommandSource(), player)) {
                player.currentScreenHandler.sendContentUpdates();
            }

            player.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(hand));
        }

        return TypedActionResult.success(stack, true);
    }
}
