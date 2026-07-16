package io.github.redstoneparadox.creeperfall.game.participant;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.Objects;

public class CreeperfallParticipant {
    private final PlayerRef player;
	private final ServerLevel world;
	private boolean gameStarted = false;
    private boolean fireworks = false;

    public final ArmorUpgrade armorUpgrade;

    public final StatUpgrade maxArrowsUpgrade;

	public CreeperfallParticipant(PlayerRef player, ServerLevel world, CreeperfallConfig config) {
		this.armorUpgrade = new ArmorUpgrade.Builder(world.registryAccess())
				.tier(ArmorUpgrade.ArmorType.NONE)
				.tier(ArmorUpgrade.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 1)
				.tier(ArmorUpgrade.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 2)
				.tier(ArmorUpgrade.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 3)
				.build();
		this.player = player;
		this.world = world;
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

	@Nullable
	public ServerPlayer getPlayerEntity() {
		return getPlayer().getEntity(this.world);
	}

	public ServerLevel getLevel() {
		return this.world;
	}

	public void replenishArrows() {
		if (!gameStarted) return;

		Player player = getPlayer().getEntity(this.world);
		Inventory inventory = Objects.requireNonNull(player).getInventory();

		int maxArrowsTier = maxArrowsUpgrade.getTier();

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			Item item = inventory.getItem(i).getItem();
			if (item == Items.ARROW || item == Items.FIREWORK_ROCKET) {
				inventory.setItem(i, ItemStack.EMPTY);
			}
		}

		Item cursorItem = player.containerMenu.getCarried().getItem();
		if (cursorItem == Items.ARROW || cursorItem == Items.FIREWORK_ROCKET) {
			player.containerMenu.setCarried(ItemStack.EMPTY);
		}

		if (fireworks) {
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FIREWORK_ROCKET, maxArrowsUpgrade.getValue(maxArrowsTier)));
		} else {
			player.addItem(new ItemStack(Items.ARROW, maxArrowsUpgrade.getValue(maxArrowsTier)));
		}
	}

	public void notifyOfStart() {
		gameStarted = true;
	}

	public void enableCrossbowAndFireworks() {
		fireworks = true;

		Player player = getPlayer().getEntity(this.world);
		Inventory inventory = Objects.requireNonNull(player).getInventory();

		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (inventory.getItem(i).getItem() == Items.BOW) {
				inventory.setItem(i, ItemStack.EMPTY);
			}
		}

		if (player.containerMenu.getCarried().getItem() == Items.BOW) {
			player.containerMenu.setCarried(ItemStack.EMPTY);
		}

		ItemStack crossbowStack = new ItemStack(Items.CROSSBOW);
		crossbowStack.enchant(this.world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.QUICK_CHARGE), 3);

		player.addItem(crossbowStack);
		replenishArrows();
	}
}
