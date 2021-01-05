package io.github.redstoneparadox.creeperfall.item;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CreeperfallItems {
	public static void init() {

	}

	private static void register(String path, Item item) {
		Identifier identifier = new Identifier(Creeperfall.ID, path);

		Registry.register(Registry.ITEM, identifier, item);
	}
}
