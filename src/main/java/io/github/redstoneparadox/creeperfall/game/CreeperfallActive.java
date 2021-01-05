package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.shop.CreeperfallShop;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallCreeperSpawnLogic;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallPlayerSpawnLogic;
import io.github.redstoneparadox.creeperfall.game.util.Timer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CreeperfallActive {
    private final CreeperfallConfig config;

    public final GameSpace gameSpace;
    private final CreeperfallMap gameMap;
    private final Random random = new Random();

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final Object2ObjectMap<PlayerRef, CreeperfallParticipant> participants;
    private final CreeperfallPlayerSpawnLogic playerSpawnLogic;
    private final CreeperfallCreeperSpawnLogic creeperSpawnLogic;
    private final CreeperfallStageManager stageManager;
    private final boolean ignoreWinState;
    private final CreeperfallTimerBar timerBar;
    private final Timer arrowReplenishTimer;

    private CreeperfallActive(GameSpace gameSpace, CreeperfallMap map, GlobalWidgets widgets, CreeperfallConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.playerSpawnLogic = new CreeperfallPlayerSpawnLogic(gameSpace, map, config);
        this.creeperSpawnLogic = new CreeperfallCreeperSpawnLogic(gameSpace, map, config);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (PlayerRef player : participants) {
            this.participants.put(player, new CreeperfallParticipant(player, gameSpace));
        }

        this.stageManager = new CreeperfallStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new CreeperfallTimerBar(widgets);
        int arrowReplenishTime = config.arrowReplenishTimeSeconds * 20;
        this.arrowReplenishTimer = Timer.createRepeating(arrowReplenishTime, this::onReplenishArrows);
    }

    public static void open(GameSpace gameSpace, CreeperfallMap map, CreeperfallConfig config) {
        gameSpace.openGame(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = new GlobalWidgets(game);
            CreeperfallActive active = new CreeperfallActive(gameSpace, map, widgets, config, participants);

            gameSpace.getWorld().getGameRules().get(GameRules.NATURAL_REGENERATION).set(false, gameSpace.getServer());

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
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
            game.on(UseItemListener.EVENT, active::onUseItem);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private void onReplenishArrows() {
        for (ServerPlayerEntity player : gameSpace.getPlayers()) {
            PlayerInventory inventory = player.inventory;

            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).getItem() == Items.ARROW) {
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }

            if (inventory.getCursorStack().getItem() == Items.ARROW) {
                inventory.setCursorStack(ItemStack.EMPTY);
            }

            player.giveItemStack(new ItemStack(Items.ARROW, config.maxArrows));
        }
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
        //this.spawnParticipant(player);
        return ActionResult.PASS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        // TODO handle death
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.playerSpawnLogic.resetPlayer(player, GameMode.SURVIVAL);
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
        creeperSpawnLogic.tick();
        arrowReplenishTimer.tick();
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

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() == Items.COMPASS) {
            player.openHandledScreen(CreeperfallShop.create(participants.get(PlayerRef.of(player)), this, config.shopConfig));
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
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
