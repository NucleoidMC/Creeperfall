package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemUpgrade implements Upgrade<ItemStack> {
	private final List<ItemStack> tiers;

	private int currentTier = -1;

	public ItemUpgrade(List<ItemStack> tiers) {
		this.tiers = tiers;
	}

	@Override
	public boolean canUpgrade() {
		return currentTier < tiers.size() - 1;
	}

	@Override
	public int getTier() {
		return currentTier;
	}

	@Override
	public ItemStack getValue(int tier) {
		return tiers.get(tier);
	}

	@Override
	public boolean upgrade(CreeperfallParticipant participant) {
		ServerWorld world = participant.getWorld();
		ServerPlayerEntity player = participant.getPlayer().getEntity(world);
		PlayerInventory inventory = Objects.requireNonNull(player).getInventory();

		if (currentTier + 1 >= tiers.size()) return false;

		currentTier += 1;

		if (currentTier == 0) {
			player.giveItemStack(tiers.get(0).copy());
		}

		for (int slot = 0; slot < inventory.size(); slot++) {
			if (ItemStack.areEqual(inventory.getStack(slot), tiers.get(currentTier - 1))) {
				inventory.setStack(slot, tiers.get(currentTier));
				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack getIcon() {
		return tiers.get(currentTier);
	}

	public static class Builder {
		private final List<ItemStack> tiers = new ArrayList<>();

		public Builder tier(ItemStack stack) {
			tiers.add(stack);
			return this;
		}

		public ItemUpgrade build() {
			return new ItemUpgrade(tiers);
		}
	}
}
