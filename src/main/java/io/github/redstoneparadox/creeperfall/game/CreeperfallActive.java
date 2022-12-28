package io.github.redstoneparadox.creeperfall.game;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import io.github.redstoneparadox.creeperfall.entity.CreeperfallGuardianEntity;
import io.github.redstoneparadox.creeperfall.entity.CreeperfallOcelotEntity;
import io.github.redstoneparadox.creeperfall.entity.CreeperfallSkeletonEntity;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
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
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.entity.EntityDeathEvent;
import xyz.nucleoid.stimuli.event.entity.EntityDropItemsEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.projectile.ProjectileHitEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CreeperfallActive {
    private final CreeperfallConfig config;

    public final GameSpace gameSpace;
    private final CreeperfallMap gameMap;
    private final Random random = Random.create();

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final EntityTracker tracker;
    private final Object2ObjectMap<PlayerRef, CreeperfallParticipant> participants;
    private final CreeperfallPlayerSpawnLogic playerSpawnLogic;
    private final CreeperfallCreeperSpawnLogic creeperSpawnLogic;
    private final CreeperfallStageManager stageManager;
    private final boolean ignoreWinState;
    private final CreeperfallTimerBar timerBar;
    private final Timer arrowReplenishTimer;
    private final ServerWorld world;

    private CreeperfallActive(GameSpace gameSpace, ServerWorld world, CreeperfallMap map, GlobalWidgets widgets, CreeperfallConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.config = config;
        this.gameMap = map;
        this.tracker = new EntityTracker();
        this.playerSpawnLogic = new CreeperfallPlayerSpawnLogic(world, map);
        this.creeperSpawnLogic = new CreeperfallCreeperSpawnLogic(gameSpace, world,this, map, config, tracker);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (PlayerRef player : participants) {
            this.participants.put(player, new CreeperfallParticipant(player, world, config));
        }

        this.stageManager = new CreeperfallStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new CreeperfallTimerBar(widgets);
        int arrowReplenishTime = config.arrowReplenishTimeSeconds * 20;
        this.arrowReplenishTimer = Timer.createRepeating(arrowReplenishTime, this::onReplenishArrows);
    }

    public static void open(GameSpace gameSpace, ServerWorld world, CreeperfallMap map, CreeperfallConfig config) {
        gameSpace.setActivity(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            CreeperfallActive active = new CreeperfallActive(gameSpace, world, map, widgets, config, participants);

            game.setRule(GameRuleType.CRAFTING, ActionResult.FAIL);
            game.setRule(GameRuleType.PORTALS, ActionResult.FAIL);
            game.setRule(GameRuleType.PVP, ActionResult.FAIL);
            game.setRule(GameRuleType.HUNGER, ActionResult.FAIL);
            game.setRule(GameRuleType.FALL_DAMAGE, ActionResult.FAIL);
            game.setRule(GameRuleType.BLOCK_DROPS, ActionResult.FAIL);
            game.setRule(GameRuleType.THROW_ITEMS, ActionResult.FAIL);
            game.setRule(GameRuleType.UNSTABLE_TNT, ActionResult.FAIL);
            game.setRule(GameRuleType.BREAK_BLOCKS, ActionResult.FAIL);

            game.listen(GameActivityEvents.ENABLE, active::onOpen);
            game.listen(GameActivityEvents.DISABLE, active::onClose);

            game.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, Vec3d.ZERO));
            game.listen(GamePlayerEvents.ADD, active::addPlayer);
            game.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            game.listen(GameActivityEvents.TICK, active::tick);
            game.listen(ExplosionDetonatedEvent.EVENT, active::onExplosion);
            game.listen(EntityDeathEvent.EVENT, active::onEntityDeath);
            game.listen(EntityDropItemsEvent.EVENT, active::onDropLoot);
            game.listen(ItemUseEvent.EVENT, active::onUseItem);

            game.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            game.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            game.listen(PlayerAttackEntityEvent.EVENT, active::onAttackEntity);
            game.listen(ProjectileHitEvent.ENTITY, active::onEntityHit);
        });
    }

    public void announceStage(int stage) {
        PlayerSet players = gameSpace.getPlayers();
        players.showTitle(Text.translatable("game.creeperfall.stage", stage), 5, 40, 5);
    }

    public void spawnGuardian() {
        CreeperfallGuardianEntity entity = new CreeperfallGuardianEntity(this.world);

        entity.setInvulnerable(true);
        spawnEntity(entity, 0.5, 68, 0.5, SpawnReason.SPAWN_EGG);
    }

    public void spawnOcelot() {
        CreeperfallOcelotEntity entity = new CreeperfallOcelotEntity(tracker, this.world);

        entity.setInvulnerable(true);
        spawnEntity(entity, 0.5, 65, 0.5, SpawnReason.SPAWN_EGG);
    }

    public void spawnSkeleton() {
        CreeperfallSkeletonEntity entity = new CreeperfallSkeletonEntity(this.world);

        spawnEntity(entity, 0.5, 65, 0.5, SpawnReason.SPAWN_EGG);
    }

    public void spawnEntity(Entity entity, double x, double y, double z, SpawnReason spawnReason) {

        if (this.world != entity.world) {
            Creeperfall.LOGGER.error("Attempted to add an entity to Creeperfall's gamespace that was not in the correct ServerWorld.");
            return;
        }

        Objects.requireNonNull(entity).setPos(x, y, z);
        entity.updatePosition(x, y, z);
        entity.setVelocity(Vec3d.ZERO);

        entity.prevX = x;
        entity.prevY = y;
        entity.prevZ = z;

        if (entity instanceof MobEntity) {
            ((MobEntity) entity).initialize(world, world.getLocalDifficulty(new BlockPos(0, 0, 0)), spawnReason, null, null);
        }

        world.spawnEntity(entity);
        tracker.add(entity);
    }

    private void onReplenishArrows() {
        for (CreeperfallParticipant participant: participants.values()) {
            participant.replenishArrows();
        }
    }

    private void onOpen() {
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(world, this::spawnParticipant);
        }
        this.stageManager.onOpen(this.world.getTime(), this.config);
    }

    private void onClose() {

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
        Entity sourceEntity = source.getSource();

        if (sourceEntity instanceof ArrowEntity) {
            Entity owner = ((ArrowEntity) sourceEntity).getOwner();

            if (owner instanceof SkeletonEntity) {
                return ActionResult.FAIL;
            }
        }

        // TODO handle damage
        //this.spawnParticipant(player);
        return ActionResult.PASS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.removePlayer(player);
        this.spawnSpectator(player);

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(source.getDeathMessage(player));

        return ActionResult.FAIL;
    }

    private ActionResult onAttackEntity(ServerPlayerEntity attacker, Hand hand, Entity attacked, EntityHitResult hitResult) {
        if (!(attacked instanceof CreeperEntity)) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    private ActionResult onEntityHit(ProjectileEntity entity, EntityHitResult hitResult) {
        if (!(hitResult.getEntity() instanceof CreeperEntity)) return ActionResult.FAIL;
        return ActionResult.PASS;
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
            case GAME_STARTED:
                for (CreeperfallParticipant participant: participants.values()) {
                    participant.notifyOfStart();
                    participant.replenishArrows();
                }
                return;
            case GAME_FINISHED:
                this.broadcastResult(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
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

    private void onExplosion(Explosion explosion, boolean bool) {
        explosion.clearAffectedBlocks();
    }

    private ActionResult onEntityDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof CreeperEntity && source instanceof EntityDamageSource) {
            @Nullable Entity sourceEntity = source.getSource();
            @Nullable ServerPlayerEntity player = null;

            if (sourceEntity instanceof ServerPlayerEntity && this.world.getEntityById(sourceEntity.getId()) != null) {
                player = (ServerPlayerEntity) sourceEntity;
            }
            else if (sourceEntity instanceof ArrowEntity) {
                Entity owner = ((ArrowEntity)sourceEntity).getOwner();

                if (owner instanceof ServerPlayerEntity && this.world.getEntityById(sourceEntity.getId()) != null) {
                    player = (ServerPlayerEntity) owner;
                }
            }

            if (player != null) {
                int maxEmeralds = config.emeraldRewardCount.getMax();
                int minEmeralds = config.emeraldRewardCount.getMin();
                int emeralds = (random.nextInt(maxEmeralds - minEmeralds) + 1) + minEmeralds;
                player.giveItemStack(new ItemStack(Items.EMERALD, emeralds));
                player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
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
            CreeperfallShop.create(participants.get(PlayerRef.of(player)), this, config.shopConfig);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    private void broadcastResult(GameResult result) {
        boolean survivors = result.survivors;

        Text message;
        SoundEvent sound;
        if (survivors) {
            List<CreeperfallParticipant> survivorsList = new ArrayList<>(participants.values());


            if (survivorsList.size() == 1) {
                ServerPlayerEntity playerEntity = survivorsList.get(0).getPlayer().getEntity(world);
                assert playerEntity != null;
                message = Text.translatable("game.creeperfall.end.success.one", playerEntity.getDisplayName().copy());
            }
            else if (survivorsList.size() == 2) {
                ServerPlayerEntity playerEntityOne = survivorsList.get(0).getPlayer().getEntity(world);
                ServerPlayerEntity playerEntityTwo = survivorsList.get(1).getPlayer().getEntity(world);
                assert playerEntityOne != null;
                assert playerEntityTwo != null;
                message = Text.translatable("game.creeperfall.end.success.multiple", playerEntityOne.getDisplayName().copy(), playerEntityTwo.getDisplayName().copy());
            }
            else {
                List<CreeperfallParticipant> firstSurvivorsList = survivorsList.subList(0, survivorsList.size() - 1);
                MutableText survivorsText = Text.empty();

                for (CreeperfallParticipant survivor: firstSurvivorsList) {
                    ServerPlayerEntity playerEntity = survivor.getPlayer().getEntity(world);
                    assert playerEntity != null;
                    survivorsText.append(playerEntity.getDisplayName().copy());
                    survivorsText.append(", ");
                }

                ServerPlayerEntity playerEntityLast = survivorsList.get(survivorsList.size() - 1).getPlayer().getEntity(world);
                assert playerEntityLast != null;
                message = Text.translatable("game.creeperfall.end.success.multiple", survivorsText, playerEntityLast.getDisplayName().copy());
            }

            sound = SoundEvents.ENTITY_VILLAGER_CELEBRATE;

        } else {
            message = Text.translatable("game.creeperfall.end.fail").formatted(Formatting.RED);
            sound = SoundEvents.ENTITY_VILLAGER_NO;
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(sound);
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
