package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.enchantment.Enchantments;
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

	public CreeperfallParticipant(PlayerRef player, GameSpace gameSpace) {
		this.player = player;
		this.gameSpace = gameSpace;

		armorUpgrade.upgrade(this);
	}

	public PlayerRef getPlayer() {
		return player;
	}

	public GameSpace getGameSpace() {
		return gameSpace;
	}
}
