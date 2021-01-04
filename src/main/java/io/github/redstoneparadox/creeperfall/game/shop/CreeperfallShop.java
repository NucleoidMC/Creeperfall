package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.item.CreeperfallItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopUi;

public class CreeperfallShop {
	public static ShopUi create(ServerPlayerEntity player, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.addItem(guardianSpawnEgg(), Cost.ofEmeralds(shopConfig.guardianEggPrice));
		});
	}

	private static ItemStack guardianSpawnEgg() {
		ItemStack stack = new ItemStack(CreeperfallItems.GUARDIAN_SPAWN_EGG);
		CompoundTag nbt = stack.getOrCreateSubTag("display");

		nbt.putString("Lore", new TranslatableText("item.creeperfall.guardian_spawn_egg.lore").asString());

		return stack;
	}
}
