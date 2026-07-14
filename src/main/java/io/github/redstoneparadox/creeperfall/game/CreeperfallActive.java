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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.api.util.PlayerUtil;
import xyz.nucleoid.stimuli.event.DroppedItemsResult;
import xyz.nucleoid.stimuli.event.EventResult;
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
    private final RandomSource random = RandomSource.create();

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final EntityTracker tracker;
    private final Object2ObjectMap<PlayerRef, CreeperfallParticipant> participants;
    private final CreeperfallPlayerSpawnLogic playerSpawnLogic;
    private final CreeperfallCreeperSpawnLogic creeperSpawnLogic;
    private final CreeperfallStageManager stageManager;
    private final CreeperfallTimerBar timerBar;
    private final Timer arrowReplenishTimer;
    private final ServerLevel level;
    private boolean hasPlayerDied = false;

    private CreeperfallActive(GameSpace gameSpace, ServerLevel level, CreeperfallMap map, GlobalWidgets widgets, CreeperfallConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.level = level;
        this.config = config;
        this.gameMap = map;
        this.tracker = new EntityTracker();
        this.playerSpawnLogic = new CreeperfallPlayerSpawnLogic(level, map);
        this.creeperSpawnLogic = new CreeperfallCreeperSpawnLogic(gameSpace, level,this, map, config, tracker);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (PlayerRef player : participants) {
            this.participants.put(player, new CreeperfallParticipant(player, level, config));
        }

        this.stageManager = new CreeperfallStageManager();
        this.timerBar = new CreeperfallTimerBar(widgets);
        int arrowReplenishTime = config.arrowReplenishTimeSeconds * 20;
        this.arrowReplenishTimer = Timer.createRepeating(arrowReplenishTime, this::onReplenishArrows);
    }

    public static void open(GameSpace gameSpace, ServerLevel level, CreeperfallMap map, CreeperfallConfig config) {
        gameSpace.setActivity(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().participants().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            CreeperfallActive active = new CreeperfallActive(gameSpace, level, map, widgets, config, participants);

            game.setRule(GameRuleType.CRAFTING, EventResult.DENY);
            game.setRule(GameRuleType.PORTALS, EventResult.DENY);
            game.setRule(GameRuleType.PVP, EventResult.DENY);
            game.setRule(GameRuleType.HUNGER, EventResult.DENY);
            game.setRule(GameRuleType.FALL_DAMAGE, EventResult.DENY);
            game.setRule(GameRuleType.BLOCK_DROPS, EventResult.DENY);
            game.setRule(GameRuleType.THROW_ITEMS, EventResult.DENY);
            game.setRule(GameRuleType.UNSTABLE_TNT, EventResult.DENY);
            game.setRule(GameRuleType.BREAK_BLOCKS, EventResult.DENY);

            game.listen(GameActivityEvents.ENABLE, active::onOpen);
            game.listen(GameActivityEvents.DISABLE, active::onClose);
            game.listen(GameActivityEvents.STATE_UPDATE, state -> state.canPlay(false));

            game.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(level, Vec3.atBottomCenterOf(map.spawn)));
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
        players.showTitle(Component.translatable("game.creeperfall.stage", stage), 5, 40, 5);
    }

    public void spawnGuardian() {
        CreeperfallGuardianEntity entity = new CreeperfallGuardianEntity(this.level);

        entity.setInvulnerable(true);
        spawnEntity(entity, 0.5, 68, 0.5, EntitySpawnReason.SPAWN_ITEM_USE);
    }

    public void spawnOcelot() {
        CreeperfallOcelotEntity entity = new CreeperfallOcelotEntity(tracker, this.level);

        entity.setInvulnerable(true);
        spawnEntity(entity, 0.5, 65, 0.5, EntitySpawnReason.SPAWN_ITEM_USE);
    }

    public void spawnSkeleton() {
        CreeperfallSkeletonEntity entity = new CreeperfallSkeletonEntity(this.level);

        spawnEntity(entity, 0.5, 65, 0.5, EntitySpawnReason.SPAWN_ITEM_USE);
    }

    public void spawnEntity(Entity entity, double x, double y, double z, EntitySpawnReason spawnReason) {

        if (this.level != entity.level()) {
            Creeperfall.LOGGER.error("Attempted to add an entity to Creeperfall's gamespace that was not in the correct ServerWorld.");
            return;
        }

        Objects.requireNonNull(entity).setPosRaw(x, y, z);
        entity.absSnapTo(x, y, z);
        entity.setDeltaMovement(Vec3.ZERO);

        entity.xo = x;
        entity.yo = y;
        entity.zo = z;

        if (entity instanceof Mob) {
            ((Mob) entity).finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(0, 0, 0)), spawnReason, null);
        }

        level.addFreshEntity(entity);
        tracker.add(entity);
    }

    private void onReplenishArrows() {
        for (CreeperfallParticipant participant: participants.values()) {
            participant.replenishArrows();
        }
    }

    private void onOpen() {
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(level, this::spawnParticipant);
        }
        this.stageManager.onOpen(this.level.getGameTime(), this.config);
    }

    private void onClose() {

    }

    private void addPlayer(ServerPlayer player) {
        if (!this.participants.containsKey(PlayerRef.of(player))) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayer player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
        Entity sourceEntity = source.getDirectEntity();

        if (sourceEntity instanceof Arrow) {
            Entity owner = ((Arrow) sourceEntity).getOwner();

            if (owner instanceof Skeleton) {
                return EventResult.DENY;
            }
        }

        // TODO handle damage
        //this.spawnParticipant(player);
        return EventResult.PASS;
    }

    private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
        this.removePlayer(player);
        this.spawnSpectator(player);

        PlayerSet players = this.gameSpace.getPlayers();
        hasPlayerDied = true;

        players.sendMessage(source.getLocalizedDeathMessage(player));

        return EventResult.DENY;
    }

    private EventResult onAttackEntity(ServerPlayer attacker, InteractionHand hand, Entity attacked, EntityHitResult hitResult) {
        if (!(attacked instanceof Creeper)) return EventResult.DENY;
        return EventResult.PASS;
    }

    private EventResult onEntityHit(Projectile entity, EntityHitResult hitResult) {
        if (!(hitResult.getEntity() instanceof Creeper)) return EventResult.DENY;
        return EventResult.PASS;
    }

    private void spawnParticipant(ServerPlayer player) {
        this.playerSpawnLogic.resetPlayer(player, GameType.SURVIVAL, false);
        this.playerSpawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayer player) {
        this.playerSpawnLogic.resetPlayer(player, GameType.SPECTATOR, false);
        this.playerSpawnLogic.spawnPlayer(player);
    }

    private void tick() {
        tracker.clean();
        boolean finishedEarly = participants.isEmpty();

        long time = level.getGameTime();

        if (finishedEarly) {
            long remainingTime = this.stageManager.finishTime - level.getGameTime();
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
                this.broadcastResult();
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        if (finishedEarly) {
            this.timerBar.update(0, this.config.timeLimitSecs * 20L);
        }
        else {
            this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20L);
            creeperSpawnLogic.tick();
            arrowReplenishTimer.tick();
        }
    }



    private EventResult onExplosion(Explosion explosion, List<BlockPos> blockPos) {
        blockPos.clear();

        return EventResult.PASS;
    }

    private EventResult onEntityDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof Creeper) {
            @Nullable Entity sourceEntity = source.getDirectEntity();
            @Nullable ServerPlayer player = null;

            if (sourceEntity instanceof ServerPlayer && this.level.getEntity(sourceEntity.getId()) != null) {
                player = (ServerPlayer) sourceEntity;
            }
            else if (sourceEntity instanceof Arrow) {
                Entity owner = ((Arrow)sourceEntity).getOwner();

                if (owner instanceof ServerPlayer && this.level.getEntity(sourceEntity.getId()) != null) {
                    player = (ServerPlayer) owner;
                }
            }

            if (player != null) {
                int maxEmeralds = config.emeraldRewardCount.max().orElse(1024);
                int minEmeralds = config.emeraldRewardCount.min().orElse(0);
                int emeralds = (random.nextInt(maxEmeralds - minEmeralds) + 1) + minEmeralds;
                player.addItem(new ItemStack(Items.EMERALD, emeralds));
                PlayerUtil.playSoundToPlayer(player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1.0f, 1.0f);
            }
        }

        return EventResult.PASS;
    }

    private DroppedItemsResult onDropLoot(LivingEntity dropper, List<ItemStack> loot) {
        loot.clear();
        return DroppedItemsResult.pass(loot);
    }

    private InteractionResult onUseItem(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() == Items.COMPASS) {
            CreeperfallShop.create(participants.get(PlayerRef.of(player)), this, config.shopConfig);
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    private void broadcastResult() {
        Component message = Component.translatable("game.creeperfall.end.success.all");
        SoundEvent sound = SoundEvents.VILLAGER_CELEBRATE;

        if (hasPlayerDied) {
            if (!participants.isEmpty()) {
                List<CreeperfallParticipant> survivorsList = new ArrayList<>(participants.values());

                if (survivorsList.size() == 1) {
                    ServerPlayer playerEntity = survivorsList.get(0).getPlayer().getEntity(level);
                    assert playerEntity != null;
                    message = Component.translatable("game.creeperfall.end.success.one", playerEntity.getDisplayName().copy());
                }
                else if (survivorsList.size() == 2) {
                    ServerPlayer playerEntityOne = survivorsList.get(0).getPlayer().getEntity(level);
                    ServerPlayer playerEntityTwo = survivorsList.get(1).getPlayer().getEntity(level);
                    assert playerEntityOne != null;
                    assert playerEntityTwo != null;
                    message = Component.translatable("game.creeperfall.end.success.multiple", playerEntityOne.getDisplayName().copy(), playerEntityTwo.getDisplayName().copy());
                }
                else {
                    List<CreeperfallParticipant> firstSurvivorsList = survivorsList.subList(0, survivorsList.size() - 1);
                    MutableComponent survivorsText = Component.empty();

                    for (CreeperfallParticipant survivor: firstSurvivorsList) {
                        ServerPlayer playerEntity = survivor.getPlayer().getEntity(level);
                        assert playerEntity != null;
                        survivorsText.append(playerEntity.getDisplayName().copy());
                        survivorsText.append(", ");
                    }

                    ServerPlayer playerEntityLast = survivorsList.get(survivorsList.size() - 1).getPlayer().getEntity(level);
                    assert playerEntityLast != null;
                    message = Component.translatable("game.creeperfall.end.success.multiple", survivorsText, playerEntityLast.getDisplayName().copy());
                }
            } else {
                message = Component.translatable("game.creeperfall.end.fail").withStyle(ChatFormatting.RED);
                sound = SoundEvents.VILLAGER_NO;
            }
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(sound);
    }
}
