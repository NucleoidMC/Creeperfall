package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.game.util.Timer;
import io.github.redstoneparadox.creeperfall.mixin.MobEntityAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.util.PlayerRef;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;

import java.util.*;
import java.util.stream.Collectors;

public class CreeperfallActive {
    private final CreeperfallConfig config;

    public final GameSpace gameSpace;
    private final CreeperfallMap gameMap;
    private Timer spawnTimer = Timer.createRepeating(100, this::spawnCreeper);
    private final Random random = new Random();

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final Object2ObjectMap<PlayerRef, CreeperfallPlayer> participants;
    private final CreeperfallPlayerSpawnLogic playerSpawnLogic;
    private final CreeperfallStageManager stageManager;
    private final boolean ignoreWinState;
    private final CreeperfallTimerBar timerBar;

    private CreeperfallActive(GameSpace gameSpace, CreeperfallMap map, GlobalWidgets widgets, CreeperfallConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.playerSpawnLogic = new CreeperfallPlayerSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (PlayerRef player : participants) {
            this.participants.put(player, new CreeperfallPlayer());
        }

        this.stageManager = new CreeperfallStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new CreeperfallTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, CreeperfallMap map, CreeperfallConfig config) {
        gameSpace.openGame(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = new GlobalWidgets(game);
            CreeperfallActive active = new CreeperfallActive(gameSpace, map, widgets, config, participants);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);
            game.setRule(GameRule.BREAK_BLOCKS, RuleResult.DENY);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);
            game.on(ExplosionListener.EVENT, active::onExplosion);
            game.on(EntityDeathListener.EVENT, active::onEntityDeath);
            game.on(EntityDropLootListener.EVENT, active::onDropLoot);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private void spawnCreeper() {
        ServerWorld world = gameSpace.getWorld();
        CreeperEntity entity = EntityType.CREEPER.create(world);

        int size = config.mapConfig.size;
        int radius = size/2;
        int x = random.nextInt(size) - radius;
        int y = 85;
        int z = random.nextInt(size) - radius;

        Objects.requireNonNull(entity).setPos(0, 85, 0);
        entity.updatePosition(x, y, z);
        entity.setVelocity(Vec3d.ZERO);

        entity.prevX = x;
        entity.prevY = y;
        entity.prevZ = z;

        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 1, true, false));
        entity.initialize(world, world.getLocalDifficulty(new BlockPos(0, 0, 0)), SpawnReason.NATURAL, null, null);
        entity.setHealth(0.5f);
        ((MobEntityAccessor) entity).setExperiencePoints(0);
        world.spawnEntity(entity);
    }

    private void onOpen() {
        ServerWorld world = this.gameSpace.getWorld();
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(world, this::spawnParticipant);
        }
        this.stageManager.onOpen(world.getTime(), this.config);
        // TODO setup logic
    }

    private void onClose() {
        // TODO teardown logic
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(PlayerRef.of(player))) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        // TODO handle damage
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        // TODO handle death
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.playerSpawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.playerSpawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.playerSpawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.playerSpawnLogic.spawnPlayer(player);
    }

    private void tick() {
        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();

        CreeperfallStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close();
                return;
        }

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20);
        this.spawnTimer.tick();
    }

    private void onExplosion(List<BlockPos> affectedBlocks) {
        affectedBlocks.clear();
    }

    private ActionResult onEntityDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof CreeperEntity && source instanceof EntityDamageSource) {
            @Nullable Entity sourceEntity = source.getSource();

            if (sourceEntity instanceof ServerPlayerEntity && gameSpace.containsEntity(sourceEntity)) {
                int emeralds = random.nextInt(3) + 1;

                ((ServerPlayerEntity) sourceEntity).giveItemStack(new ItemStack(Items.EMERALD, emeralds));
            }
            else if (sourceEntity instanceof ArrowEntity) {
                Entity owner = ((ArrowEntity)sourceEntity).getOwner();

                if (owner instanceof ServerPlayerEntity && gameSpace.containsEntity(owner)) {
                    int emeralds = random.nextInt(3) + 1;

                    ((ServerPlayerEntity) owner).giveItemStack(new ItemStack(Items.EMERALD, emeralds));
                }
            }
        }

        return ActionResult.PASS;
    }

    private TypedActionResult<List<ItemStack>> onDropLoot(LivingEntity dropper, List<ItemStack> loot) {
        loot.clear();
        return TypedActionResult.consume(loot);
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerWorld world = this.gameSpace.getWorld();
        ServerPlayerEntity winningPlayer = null;

        // TODO win result logic
        return WinResult.no();
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
