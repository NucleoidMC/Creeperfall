package io.github.redstoneparadox.creeperfall.game;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import io.github.redstoneparadox.creeperfall.Creeperfall;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;

public class CreeperfallSpawnLogic {
    private final GameSpace gameSpace;
    private final CreeperfallMap map;

    public CreeperfallSpawnLogic(GameSpace gameSpace, CreeperfallMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.setGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.inventory.clear();

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));

        ItemStack bowStack = new ItemStack(Items.BOW);
        CompoundTag nbt = bowStack.getOrCreateTag();

        nbt.putBoolean("Unbreakable", true);

        player.giveItemStack(bowStack);
        player.giveItemStack(new ItemStack(Items.ARROW, 10));
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        ServerWorld world = this.gameSpace.getWorld();

        BlockPos pos = this.map.spawn;
        if (pos == null) {
            Creeperfall.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        float radius = 4.5f;
        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), -radius, radius);
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), -radius, radius);

        player.teleport(world, x, pos.getY() + 0.5, z, 0.0F, 0.0F);
    }
}
