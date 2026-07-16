package io.github.redstoneparadox.creeperfall.game.participant;

import io.github.redstoneparadox.creeperfall.game.util.Tuple;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArmorUpgrade implements Upgrade<List<ItemStack>> {
	private final List<Tuple<ArmorType, Consumer<List<ItemStack>>>> tiers;

	private int currentTier = -1;

	public ArmorUpgrade(List<Tuple<ArmorType, Consumer<List<ItemStack>>>> tiers) {
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
	public List<ItemStack> getValue(int tier) {
		Tuple<ArmorType, Consumer<List<ItemStack>>> pair = tiers.get(tier);
		ArmorType type = pair.getA();
		Consumer<List<ItemStack>> consumer = pair.getB();
		List<ItemStack> stacks = Arrays.asList(
				new ItemStack(type.helmet),
				new ItemStack(type.chestplate),
				new ItemStack(type.leggings),
				new ItemStack(type.boots)
		);

		if (type.isNone()) {
			return stacks;
		}

		consumer.accept(stacks);
		return stacks;
	}

	@Override
	public boolean upgrade(CreeperfallParticipant participant) {
		ServerLevel level = participant.getLevel();
		ServerPlayer player = participant.getPlayer().getEntity(level);
		Inventory inventory = Objects.requireNonNull(player).getInventory();

		if (currentTier + 1 >= tiers.size()) return false;

		currentTier += 1;

		Tuple<ArmorType, Consumer<List<ItemStack>>> tier = tiers.get(currentTier);
		ArmorType type = tier.getA();
		Consumer<List<ItemStack>> consumer = tier.getB();
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
		player.setItemSlot(EquipmentSlot.HEAD, stacks.get(0));
		player.setItemSlot(EquipmentSlot.CHEST, stacks.get(1));
		player.setItemSlot(EquipmentSlot.LEGS, stacks.get(2));
		player.setItemSlot(EquipmentSlot.FEET, stacks.get(3));

		return true;
	}

	@Override
	public ItemStack getIcon() {
		if (currentTier + 1 >= tiers.size()) {
			return new ItemStack(Items.BARRIER);
		}

		Tuple<ArmorType, Consumer<List<ItemStack>>> tier = tiers.get(currentTier + 1);
		ArmorType type = tier.getA();
		Consumer<List<ItemStack>> consumer = tier.getB();
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
		private final List<Tuple<ArmorType, Consumer<List<ItemStack>>>> tiers = new ArrayList<>();
		private final HolderLookup.RegistryLookup<Enchantment> enchantment;

		public Builder(HolderLookup.Provider lookup) {
			this.enchantment = lookup.lookupOrThrow(Registries.ENCHANTMENT);
		}

		public Builder tier(ArmorType type) {
			tiers.add(new Tuple<>(type, itemStacks -> {}));
			return this;
		}

		public Builder tier(ArmorType type, ResourceKey<Enchantment> enchantment, int level) {
			tiers.add(new Tuple<>(type, itemStacks -> {
				for (ItemStack stack : itemStacks) {
					stack.enchant(this.enchantment.getOrThrow(enchantment), level);
				}
			}));
			return this;
		}

		public ArmorUpgrade build() {
			return new ArmorUpgrade(tiers);
		}
	}
}
