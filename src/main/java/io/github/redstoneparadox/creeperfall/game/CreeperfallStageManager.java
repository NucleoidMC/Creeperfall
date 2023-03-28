package io.github.redstoneparadox.creeperfall.game;

import com.google.common.collect.ImmutableSet;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Set;

public class CreeperfallStageManager {
    private long closeTime = -1;
    public long finishTime = -1;
    private long startTime = -1;
    private final Object2ObjectMap<ServerPlayerEntity, FrozenPlayer> frozen;
    private boolean setSpectator = false;
    private boolean finishEarly = false;
    private long addedTime = 0;

    public CreeperfallStageManager() {
        this.frozen = new Object2ObjectOpenHashMap<>();
    }

    public void onOpen(long time, CreeperfallConfig config) {
        this.startTime = time - (time % 20) + (4 * 20) + 19;
        this.finishTime = this.startTime + (config.timeLimitSecs * 20);
    }

    public void finishEarly(long remainingTime) {
        if (!finishEarly) {
            addedTime += remainingTime;
            finishEarly = true;
        }
    }

    public IdleTickResult tick(long time, GameSpace space) {
        time += addedTime;

        // Game has finished. Wait a few seconds before finally closing the game.
        if (this.closeTime > 0) {
            if (time >= this.closeTime) {
                return IdleTickResult.GAME_CLOSED;
            }
            return IdleTickResult.TICK_FINISHED;
        }

        // Game hasn't started yet. Display a countdown before it begins.
        if (this.startTime > time) {
            this.tickStartWaiting(time, space);
            return IdleTickResult.TICK_FINISHED;
        }

        if (this.startTime == time) {
            return IdleTickResult.GAME_STARTED;
        }

        // Game has just finished. Transition to the waiting-before-close state.
        if (time > this.finishTime || space.getPlayers().isEmpty()) {
            if (!this.setSpectator) {
                this.setSpectator = true;
                for (ServerPlayerEntity player : space.getPlayers()) {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }

            this.closeTime = time + (5 * 20);

            return IdleTickResult.GAME_FINISHED;
        }

        return IdleTickResult.CONTINUE_TICK;
    }

    private void tickStartWaiting(long time, GameSpace space) {
        float sec_f = (this.startTime - time) / 20.0f;

        if (sec_f > 1) {
            for (ServerPlayerEntity player : space.getPlayers()) {
                if (player.isSpectator()) {
                    continue;
                }

                FrozenPlayer state = this.frozen.computeIfAbsent(player, p -> new FrozenPlayer());

                if (state.lastPos == null) {
                    state.lastPos = player.getPos();
                }

                double destX = state.lastPos.x;
                double destY = state.lastPos.y;
                double destZ = state.lastPos.z;

                // Teleport without changing the pitch and yaw
                player.requestTeleport(destX, destY, destZ);
            }
        }

        int sec = (int) Math.floor(sec_f) - 1;

        if ((this.startTime - time) % 20 == 0) {
            PlayerSet players = space.getPlayers();

            if (sec > 0) {
                players.showTitle(Text.literal(Integer.toString(sec)).formatted(Formatting.BOLD), 80);
                players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                players.showTitle(Text.translatable("game.creeperfall.go").formatted(Formatting.BOLD), 80);
                players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.PLAYERS, 1.0F, 2.0F);
            }
        }
    }

    public static class FrozenPlayer {
        public Vec3d lastPos;
    }

    public enum IdleTickResult {
        CONTINUE_TICK,
        TICK_FINISHED,
        GAME_STARTED,
        GAME_FINISHED,
        GAME_CLOSED,
    }
}
