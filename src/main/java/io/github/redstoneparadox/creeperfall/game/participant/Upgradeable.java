package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.item.ItemStack;

public interface Upgradeable {
	boolean canUpgrade();

	boolean upgrade(CreeperfallParticipant participant);

	ItemStack getIcon();
}
