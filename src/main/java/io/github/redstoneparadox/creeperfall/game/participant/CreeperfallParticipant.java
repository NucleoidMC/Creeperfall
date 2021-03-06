package io.github.redstoneparadox.creeperfall.game.participant;

import io.github.redstoneparadox.creeperfall.game.config.CreeperfallConfig;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class CreeperfallParticipant {
    private final PlayerRef player;
    private final GameSpace gameSpace;

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

		this.maxArrowsUpgrade = maxArrowsUpgradeBuilder.build();
		maxArrowsUpgrade.upgrade(this);
	}

	public PlayerRef getPlayer() {
		return player;
	}

	public GameSpace getGameSpace() {
		return gameSpace;
	}
}
