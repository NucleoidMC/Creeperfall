package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class StatUpgrade implements Upgrade<Integer> {
	private final List<Integer> tiers;
	private final ItemStack icon;
	private int currentTier = -1;

	public StatUpgrade(List<Integer> tiers, ItemStack icon) {
		this.tiers = tiers;
		this.icon = icon;
	}

	@Override
	public boolean canUpgrade() {
		return currentTier < tiers.size() - 1;
	}

	@Override
	public int getTier() {
		return currentTier;
	}

	public Integer getValue(int tier) {
		return tiers.get(tier);
	}

	@Override
	public boolean upgrade(CreeperfallParticipant participant) {
		if (currentTier + 1 >= tiers.size()) return false;

		currentTier += 1;

		return true;
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	public static class Builder {
		private final List<Integer> tiers = new ArrayList<>();
		private ItemStack icon = new ItemStack(Items.BARRIER);

		public Builder tier(int i) {
			tiers.add(i);
			return this;
		}

		public Builder icon(ItemStack icon) {
			this.icon = icon;
			return this;
		}

		public StatUpgrade build() {
			return new StatUpgrade(tiers, icon);
		}
	}
}
