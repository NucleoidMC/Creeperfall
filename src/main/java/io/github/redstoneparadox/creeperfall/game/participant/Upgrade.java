package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.item.ItemStack;

public interface Upgrade {
	boolean canUpgrade();

	boolean upgrade(CreeperfallParticipant participant);

	ItemStack getIcon();
}
