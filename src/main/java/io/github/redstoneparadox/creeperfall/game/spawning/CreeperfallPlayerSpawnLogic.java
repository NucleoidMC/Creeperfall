package io.github.redstoneparadox.creeperfall.game.spawning;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import io.github.redstoneparadox.creeperfall.game.CreeperfallConfig;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameSpace;

public class CreeperfallPlayerSpawnLogic {
    private final GameSpace gameSpace;
    private final CreeperfallMap map;
    private final CreeperfallConfig config;

    public CreeperfallPlayerSpawnLogic(GameSpace gameSpace, CreeperfallMap map, CreeperfallConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode, boolean lobby) {
        player.setGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.inventory.clear();
        player.inventory.setCursorStack(ItemStack.EMPTY);

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));

        if (gameMode != GameMode.SPECTATOR && !lobby) {
            player.giveItemStack(new ItemStack(Items.COMPASS));

            ItemStack bowStack = new ItemStack(Items.BOW);
            CompoundTag nbt = bowStack.getOrCreateTag();

            nbt.putBoolean("Unbreakable", true);

            player.giveItemStack(bowStack);
            player.giveItemStack(new ItemStack(Items.ARROW, config.maxArrows));
        }

        if (lobby) {
            ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag nbt = bookStack.getOrCreateTag();
            ListTag pages = new ListTag();

            pages.add(
                    StringTag.of(
                            Text.Serializer.toJson(
                                    new LiteralText(
                                            "Gameplay takes place on a platform in the sky." +
                                                    " Creepers with slow falling with peroidically spawn above the platform with slow falling which you can shoot down with your bow and arrow." +
                                                    " Be careful, as any Creepers that land on the platform gain inviciblity, so there's no way to prevent them from exploding."
                                    )
                            )
                    )
            );
            pages.add(
                    StringTag.of(
                            Text.Serializer.toJson(
                                    new LiteralText(
                                            "The shop, which can be accessed with the compass, contains armor upgrades and can spawn a guardian or a cat." +
                                                    " The guardian will fire a laser at any creepers that get to close to the platform, save for those that have already landed." +
                                                    " The cat will kill all Creepers three seconds after spawning and despawn."
                                    )
                            )
                    )
            );
            nbt.put("pages", pages);
            nbt.putString("title", "How to Play");
            nbt.putString("author", "RedstoneParadox");

            player.giveItemStack(bookStack);
        }
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
