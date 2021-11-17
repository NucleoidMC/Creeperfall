package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class StatUpgrade implements Upgrade<Integer> {
	private final List<Integer> tiers;
	private final ItemStack icon;
	private Runnable onUpgrade;
	private int currentTier = -1;

	public StatUpgrade(List<Integer> tiers, ItemStack icon, Runnable onUpgrade) {
		this.tiers = tiers;
		this.icon = icon;
		this.onUpgrade = onUpgrade;
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
		return tiers.get(Math.min(tier, tiers.size() - 1));
	}

	@Override
	public boolean upgrade(CreeperfallParticipant participant) {
		if (currentTier + 1 >= tiers.size()) return false;

		currentTier += 1;

		onUpgrade.run();

		return true;
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	public static class Builder {
		private final List<Integer> tiers = new ArrayList<>();
		private ItemStack icon = new ItemStack(Items.BARRIER);
		private Runnable onUpgrade = () -> {};

		public Builder tier(int i) {
			tiers.add(i);
			return this;
		}

		public Builder icon(ItemStack icon) {
			this.icon = icon;
			return this;
		}

		public Builder onUpgrade(Runnable onUpgrade) {
			this.onUpgrade = onUpgrade;
			return this;
		}

		public StatUpgrade build() {
			return new StatUpgrade(tiers, icon, onUpgrade);
		}
	}
}
