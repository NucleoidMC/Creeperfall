package io.github.redstoneparadox.creeperfall.game.spawning;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import io.github.redstoneparadox.creeperfall.game.map.CreeperfallMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;

import java.util.Set;

public class CreeperfallPlayerSpawnLogic {
    private final CreeperfallMap map;
    private final ServerLevel level;

    public CreeperfallPlayerSpawnLogic(ServerLevel level, CreeperfallMap map) {
        this.level = level;
        this.map = map;
    }

    public void resetPlayer(ServerPlayer player, GameType gameMode, boolean lobby) {
        player.setGameMode(gameMode);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;
        // player.inventory.clear();
        player.containerMenu.setCarried(ItemStack.EMPTY);

        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));

        if (gameMode != GameType.SPECTATOR && !lobby) {
            ItemStack compassStack = new ItemStack(Items.COMPASS);

            compassStack.set(DataComponents.ITEM_NAME, Component.translatable("shop.creeperfall.title").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            player.addItem(compassStack);

            ItemStack bowStack = new ItemStack(Items.BOW);
            bowStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            player.addItem(bowStack);
            //player.giveItemStack(new ItemStack(Items.ARROW, config.maxArrows.get(0)));
        }

        if (lobby) {
            /*ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
            NbtCompound nbt = bookStack.getOrCreateNbt();
            NbtList pages = new NbtList();
            NbtCompound display = new NbtCompound();
            NbtList lore = new NbtList();

            pages.add(
                    NbtString.of(
                            "{\"text\":\"\",\"extra\":[\"\",{\"text\":\"Creepers:\",\"bold\":true,\"italic\":true,\"color\":\"green\"},{\"text\":\"\\nCreepers periodically fall from the sky, shoot them down before they land or they will become invincible.\\n\\n\"},{\"text\":\"Shop:\",\"bold\":true,\"italic\":true,\"color\":\"aqua\"},{\"text\":\"\\nKilling Creepers gives you emeralds to spend in the shop.\"}]}"
                    )
            );
            pages.add(
                    NbtString.of(
                            "{\"text\":\"\", \"extra\":[\"\",{\"text\":\"Survive:\",\"bold\":true,\"italic\":true,\"color\":\"gold\"},{\"text\":\"\\nThe goal is to survive to the end of the game; your health does not regen so be careful!\"}]}"
                    )
            );

            lore.add(NbtString.of("How to play Creeperfall"));
            display.put("Lore", lore);
            nbt.put("pages", pages);
            nbt.putString("title", "How to Play");
            nbt.putString("author", "RedstoneParadox");

            player.giveItemStack(bookStack);*/
        }

        if (gameMode == GameType.SPECTATOR) {
            player.getInventory().clearContent();
        }
    }

    public void spawnPlayer(ServerPlayer player) {
        BlockPos pos = this.map.spawn;
        if (pos == null) {
            Creeperfall.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        float radius = 4.5f;
        float x = pos.getX() + Mth.nextFloat(player.getRandom(), -radius, radius);
        float z = pos.getZ() + Mth.nextFloat(player.getRandom(), -radius, radius);

        player.teleportTo(this.level, x, pos.getY() + 0.5, z, Set.of(), 0.0F, 0.0F, false);
    }
}
