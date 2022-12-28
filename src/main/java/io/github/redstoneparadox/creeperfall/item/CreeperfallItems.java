package io.github.redstoneparadox.creeperfall.item;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CreeperfallItems {
	public static void init() {

	}

	private static void register(String path, Item item) {
		Identifier identifier = new Identifier(Creeperfall.ID, path);

		Registry.register(Registries.ITEM, identifier, item);
	}
}
