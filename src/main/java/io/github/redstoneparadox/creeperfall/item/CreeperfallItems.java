package io.github.redstoneparadox.creeperfall.item;

import io.github.redstoneparadox.creeperfall.Creeperfall;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CreeperfallItems {
	public static final Item GUARDIAN_SPAWN_EGG = new CreeperfallGuardianSpawnEgg();

	public static void init() {
		register("guardian_spawn_egg", GUARDIAN_SPAWN_EGG);
	}

	private static void register(String path, Item item) {
		Identifier identifier = new Identifier(Creeperfall.ID, path);

		Registry.register(Registry.ITEM, identifier, item);
	}
}
