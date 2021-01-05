package io.github.redstoneparadox.creeperfall.game.participant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Consumer;

public class ArmorUpgrade implements Upgrade {
	private final List<Pair<ArmorType, Consumer<List<ItemStack>>>> tiers;

	private int currentTier = -1;

	public ArmorUpgrade(List<Pair<ArmorType, Consumer<List<ItemStack>>>> tiers) {
		this.tiers = tiers;
	}

	@Override
	public boolean canUpgrade() {
		return currentTier < tiers.size() - 1;
	}

	@Override
	public boolean upgrade(CreeperfallParticipant participant) {
		ServerWorld world = participant.getGameSpace().getWorld();
		ServerPlayerEntity player = participant.getPlayer().getEntity(world);
		PlayerInventory inventory = Objects.requireNonNull(player).inventory;

		if (currentTier + 1 >= tiers.size()) return false;

		currentTier += 1;

		Pair<ArmorType, Consumer<List<ItemStack>>> tier = tiers.get(currentTier);
		ArmorType type = tier.getLeft();
		Consumer<List<ItemStack>> consumer = tier.getRight();
		List<ItemStack> stacks = Arrays.asList(
				new ItemStack(type.helmet),
				new ItemStack(type.chestplate),
				new ItemStack(type.leggings),
				new ItemStack(type.boots)
		);

		if (type.isNone()) {
			return true;
		};

		consumer.accept(stacks);
		player.equipStack(EquipmentSlot.HEAD, stacks.get(0));
		player.equipStack(EquipmentSlot.CHEST, stacks.get(1));
		player.equipStack(EquipmentSlot.LEGS, stacks.get(2));
		player.equipStack(EquipmentSlot.FEET, stacks.get(3));

		return true;
	}

	@Override
	public ItemStack getIcon() {
		if (currentTier + 1 >= tiers.size()) {
			return new ItemStack(Items.BARRIER);
		}

		Pair<ArmorType, Consumer<List<ItemStack>>> tier = tiers.get(currentTier + 1);
		ArmorType type = tier.getLeft();
		Consumer<List<ItemStack>> consumer = tier.getRight();
		List<ItemStack> stacks = Arrays.asList(
				new ItemStack(type.helmet),
				new ItemStack(type.chestplate),
				new ItemStack(type.leggings),
				new ItemStack(type.boots)
		);
		consumer.accept(stacks);

		return stacks.get(1);
	}

	public enum ArmorType {
		NONE(Items.AIR, Items.AIR, Items.AIR, Items.AIR),
		LEATHER(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
		CHAIN(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS),
		IRON(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
		GOLD(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS),
		DIAMOND(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);

		final Item helmet;
		final Item chestplate;
		final Item leggings;
		final Item boots;

		ArmorType(Item helmet, Item chestplate, Item leggings, Item boots) {
			this.helmet = helmet;
			this.chestplate = chestplate;
			this.leggings = leggings;
			this.boots = boots;
		}

		public boolean isNone() {
			return helmet == Items.AIR;
		}
	}

	public static class Builder {
		private final List<Pair<ArmorType, Consumer<List<ItemStack>>>> tiers = new ArrayList<>();

		public Builder tier(ArmorType type) {
			tiers.add(new Pair<>(type, itemStacks -> {}));
			return this;
		}

		public Builder tier(ArmorType type, Enchantment enchantment, int level) {
			tiers.add(new Pair<>(type, itemStacks -> {
				for (ItemStack stack : itemStacks) {
					EnchantmentHelper.set(map(enchantment, level), stack);
				}
			}));
			return this;
		}

		private <K, V> Map<K, V> map(K k, V v) {
			Map<K, V> map = new HashMap<>();
			map.put(k, v);
			return map;
		}

		public ArmorUpgrade build() {
			return new ArmorUpgrade(tiers);
		}
	}
}
