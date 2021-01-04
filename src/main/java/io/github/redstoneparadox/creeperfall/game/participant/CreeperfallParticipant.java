package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.enchantment.Enchantments;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class CreeperfallParticipant {
    private final PlayerRef player;
    private final GameSpace gameSpace;

    public final UpgradeableArmor armor = new UpgradeableArmor.Builder()
			.tier(UpgradeableArmor.ArmorType.NONE)
			.tier(UpgradeableArmor.ArmorType.CHAIN)
			.tier(UpgradeableArmor.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 1)
			.tier(UpgradeableArmor.ArmorType.CHAIN, Enchantments.BLAST_PROTECTION, 2)
			.build();

	public CreeperfallParticipant(PlayerRef player, GameSpace gameSpace) {
		this.player = player;
		this.gameSpace = gameSpace;

		armor.upgrade(this);
	}

	public PlayerRef getPlayer() {
		return player;
	}

	public GameSpace getGameSpace() {
		return gameSpace;
	}
}
