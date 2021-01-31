package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.item.ItemStack;

public interface Upgrade<T> {
	boolean canUpgrade();

	int getTier();

	T getValue(int tier);

	boolean upgrade(CreeperfallParticipant participant);

	ItemStack getIcon();
}
