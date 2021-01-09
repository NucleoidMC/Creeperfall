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
import net.minecraft.util.Formatting;
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
            ItemStack compassStack = new ItemStack(Items.COMPASS);

            compassStack.setCustomName(new LiteralText("Shop").formatted(Formatting.AQUA, Formatting.ITALIC));
            player.giveItemStack(compassStack);

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
            CompoundTag display = new CompoundTag();
            ListTag lore = new ListTag();

            pages.add(
                    StringTag.of(
                            "[\"\",{\"text\":\"Creepers:\",\"bold\":true,\"italic\":true,\"color\":\"green\"},{\"text\":\"\\nCreepers periodically fall from the sky, shoot them down before they land or they will become invincible.\\n\\n\",\"color\":\"reset\"},{\"text\":\"Shop:\",\"bold\":true,\"italic\":true,\"color\":\"aqua\"},{\"text\":\"\\nKilling Creepers gives you emeralds to spend in the shop.\",\"color\":\"reset\"}]"
                    )
            );
            pages.add(
                    StringTag.of(
                            "[\"\",{\"text\":\"Survive:\",\"bold\":true,\"italic\":true,\"color\":\"gold\"},{\"text\":\"\\nThe goal is to survive to the end of the game; your health does not regen so be careful!\",\"color\":\"reset\"}]"
                    )
            );
            lore.add(StringTag.of("How to play Creeperfall"));
            display.put("Lore", lore);
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
