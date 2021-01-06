package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.entity.CreeperfallGuardianEntity;
import io.github.redstoneparadox.creeperfall.entity.CreeperfallOcelotEntity;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.shop.CreeperfallShop;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallCreeperSpawnLogic;
import io.github.redstoneparadox.creeperfall.game.spawning.CreeperfallPlayerSpawnLogic;
import io.github.redstoneparadox.creeperfall.game.util.EntityTracker;
import io.github.redstoneparadox.creeperfall.game.util.Timer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CreeperfallActive {
    private final CreeperfallConfig config;

    public final GameSpace gameSpace;
    private final CreeperfallMap gameMap;
    private final Random random = new Random();

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final EntityTracker tracker;
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
        this.tracker = new EntityTracker();
        this.playerSpawnLogic = new CreeperfallPlayerSpawnLogic(gameSpace, map, config);
        this.creeperSpawnLogic = new CreeperfallCreeperSpawnLogic(gameSpace, map, config, tracker);
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

    public void spawnGuardian() {
        ServerWorld world = gameSpace.getWorld();
        CreeperfallGuardianEntity entity = new CreeperfallGuardianEntity(world);

        double x = 0.5;
        double y = 68;
        double z = 0.5;

        Objects.requireNonNull(entity).setPos(x, y, z);
        entity.updatePosition(x, y, z);
        entity.setVelocity(Vec3d.ZERO);

        entity.prevX = x;
        entity.prevY = y;
        entity.prevZ = z;

        entity.setInvulnerable(true);
        entity.initialize(world, world.getLocalDifficulty(new BlockPos(0, 0, 0)), SpawnReason.SPAWN_EGG, null, null);
        world.spawnEntity(entity);
        tracker.add(entity);
    }

    public void spawnOcelot() {
        ServerWorld world = gameSpace.getWorld();
        CreeperfallOcelotEntity entity = new CreeperfallOcelotEntity(tracker, world);

        double x = 0.5;
        double y = 65;
        double z = 0.5;

        Objects.requireNonNull(entity).setPos(x, y, z);
        entity.updatePosition(x, y, z);
        entity.setVelocity(Vec3d.ZERO);

        entity.prevX = x;
        entity.prevY = y;
        entity.prevZ = z;

        entity.setInvulnerable(true);
        entity.initialize(world, world.getLocalDifficulty(new BlockPos(0, 0, 0)), SpawnReason.SPAWN_EGG, null, null);
        world.spawnEntity(entity);
        tracker.add(entity);
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
        this.removePlayer(player);
        this.spawnSpectator(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.playerSpawnLogic.resetPlayer(player, GameMode.SURVIVAL, false);
        this.playerSpawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.playerSpawnLogic.resetPlayer(player, GameMode.SPECTATOR, false);
        this.playerSpawnLogic.spawnPlayer(player);
    }

    private void tick() {
        tracker.clean();
        boolean finishedEarly = participants.isEmpty();

        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();

        if (finishedEarly) {
            long remainingTime = this.stageManager.finishTime - world.getTime();
            if (remainingTime >= 0) this.stageManager.finishEarly(remainingTime);
        }

        CreeperfallStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastResult(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close();
                return;
        }

        if (finishedEarly) {
            this.timerBar.update(0, this.config.timeLimitSecs * 20);
        }
        else {
            this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20);
            creeperSpawnLogic.tick();
            arrowReplenishTimer.tick();
        }
    }

    private void onExplosion(List<BlockPos> affectedBlocks) {
        affectedBlocks.clear();
    }

    private ActionResult onEntityDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof CreeperEntity && source instanceof EntityDamageSource) {
            @Nullable Entity sourceEntity = source.getSource();
            @Nullable ServerPlayerEntity player = null;

            if (sourceEntity instanceof ServerPlayerEntity && gameSpace.containsEntity(sourceEntity)) {
                player = (ServerPlayerEntity) sourceEntity;
            }
            else if (sourceEntity instanceof ArrowEntity) {
                Entity owner = ((ArrowEntity)sourceEntity).getOwner();

                if (owner instanceof ServerPlayerEntity && gameSpace.containsEntity(owner)) {
                    player = (ServerPlayerEntity) owner;
                }
            }

            if (player != null) {
                int maxEmeralds = config.emeraldRewardCount.getMax();
                int minEmeralds = config.emeraldRewardCount.getMin();
                int emeralds = (random.nextInt(maxEmeralds - minEmeralds) + 1) + minEmeralds;
                ((ServerPlayerEntity) player).giveItemStack(new ItemStack(Items.EMERALD, emeralds));
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

    private void broadcastResult(GameResult result) {
        boolean survivors = result.survivors;

        Text message;
        if (survivors) {
            ServerWorld world = gameSpace.getWorld();
            MutableText mutableText = new LiteralText("Game completed! ");
            List<CreeperfallParticipant> survivorsList = new ArrayList<>(participants.values());

            if (survivorsList.size() > 1) {
                for (int i = 0; i < survivorsList.size(); i++) {
                    @Nullable ServerPlayerEntity playerEntity = survivorsList.get(i).getPlayer().getEntity(world);

                    if (i + 1 == survivorsList.size()) {
                        mutableText.append("and ");
                        mutableText.append(playerEntity.getDisplayName().shallowCopy());
                    }
                    else {
                        mutableText.append(playerEntity.getDisplayName().shallowCopy());
                        mutableText.append(", ");
                    }
                }

                message = mutableText.append(" have survived!").formatted(Formatting.GOLD);
            }
            else {
                @Nullable ServerPlayerEntity playerEntity = survivorsList.get(0).getPlayer().getEntity(world);
                message = mutableText.append(playerEntity.getDisplayName().shallowCopy()).append(" survived!");
            }
        } else {
            message = new LiteralText("Game Over! No one survived the Creepers...").formatted(Formatting.RED);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private GameResult checkWinResult() {
        if (participants.isEmpty()) {
            return GameResult.no();
        }

        return GameResult.survived();
    }

    static class GameResult {
        private final boolean survivors;

        private GameResult(boolean survivors) {
            this.survivors = survivors;
        }

        static GameResult no() {
            return new GameResult(false);
        }

        static GameResult survived() {
            return new GameResult(true);
        }

        public boolean isSurvivors() {
            return this.survivors;
        }
    }
}
