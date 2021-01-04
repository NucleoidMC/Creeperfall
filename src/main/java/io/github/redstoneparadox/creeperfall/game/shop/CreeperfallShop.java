package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.item.CreeperfallItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopUi;

public class CreeperfallShop {
	public static ShopUi create(ServerPlayerEntity player, CreeperfallActive game) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.addItem(new ItemStack(CreeperfallItems.GUARDIAN_SPAWN_EGG), Cost.ofEmeralds(1));
		});
	}
}
