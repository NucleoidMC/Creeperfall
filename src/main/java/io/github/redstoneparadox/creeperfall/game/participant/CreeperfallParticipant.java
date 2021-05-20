package io.github.redstoneparadox.creeperfall.game.participant;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Objects;

public class CreeperfallParticipant {
    private final PlayerRef player;
    private final GameSpace gameSpace;
    private boolean gameStarted = false;
    private boolean fireworks = false;

    public final ArmorUpgrade armorUpgrade = new ArmorUpgrade.Builder()
			.tier(ArmorUpgrade.ArmorType.NONE)
			.tier(ArmorUpgrade.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 1)
			.tier(ArmorUpgrade.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 2)
			.tier(ArmorUpgrade.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 3)
			.build();

    public final StatUpgrade maxArrowsUpgrade;

	public CreeperfallParticipant(PlayerRef player, GameSpace gameSpace, CreeperfallConfig config) {
		this.player = player;
		this.gameSpace = gameSpace;

		armorUpgrade.upgrade(this);

		StatUpgrade.Builder maxArrowsUpgradeBuilder = new StatUpgrade.Builder()
				.icon(new ItemStack(Items.ARROW));

		for (Integer maxArrows: config.maxArrows) {
			maxArrowsUpgradeBuilder.tier(maxArrows);
		}

		this.maxArrowsUpgrade = maxArrowsUpgradeBuilder
				.onUpgrade(this::replenishArrows)
				.build();
		maxArrowsUpgrade.upgrade(this);
	}

	public PlayerRef getPlayer() {
		return player;
	}

	public GameSpace getGameSpace() {
		return gameSpace;
	}

	public void replenishArrows() {
		if (!gameStarted) return;

		PlayerEntity player = getPlayer().getEntity(gameSpace.getWorld());
		PlayerInventory inventory = Objects.requireNonNull(player).inventory;

		int maxArrowsTier = maxArrowsUpgrade.getTier();

		for (int i = 0; i < inventory.size(); i++) {
			Item item = inventory.getStack(i).getItem();
			if (item == Items.ARROW || item == Items.FIREWORK_ROCKET) {
				inventory.setStack(i, ItemStack.EMPTY);
			}
		}

		Item cursorItem = inventory.getCursorStack().getItem();
		if (cursorItem == Items.ARROW || cursorItem == Items.FIREWORK_ROCKET) {
			inventory.setCursorStack(ItemStack.EMPTY);
		}

		if (fireworks) {
			player.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.FIREWORK_ROCKET, maxArrowsUpgrade.getValue(maxArrowsTier)));
		} else {
			player.giveItemStack(new ItemStack(Items.ARROW, maxArrowsUpgrade.getValue(maxArrowsTier)));
		}
	}

	public void notifyOfStart() {
		gameStarted = true;
	}

	public void enableCrossbowAndFireworks() {
		fireworks = true;

		PlayerEntity player = getPlayer().getEntity(gameSpace.getWorld());
		PlayerInventory inventory = Objects.requireNonNull(player).inventory;

		for (int i = 0; i < inventory.size(); i++) {
			if (inventory.getStack(i).getItem() == Items.BOW) {
				inventory.setStack(i, ItemStack.EMPTY);
			}
		}

		if (inventory.getCursorStack().getItem() == Items.BOW) {
			inventory.setCursorStack(ItemStack.EMPTY);
		}

		ItemStack crossbowStack = new ItemStack(Items.CROSSBOW);
		crossbowStack.addEnchantment(Enchantments.QUICK_CHARGE, 3);

		player.giveItemStack(crossbowStack);
		replenishArrows();
	}
}
