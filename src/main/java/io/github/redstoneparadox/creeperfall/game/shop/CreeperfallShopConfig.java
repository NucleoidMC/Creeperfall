package io.github.redstoneparadox.creeperfall.game.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CreeperfallShopConfig {
	public static final Codec<CreeperfallShopConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("guardian_egg_price").forGetter(config -> config.guardianEggPrice)
	).apply(instance, CreeperfallShopConfig::new));

	public final int guardianEggPrice;

	public CreeperfallShopConfig(int guardianEggPrice) {
		this.guardianEggPrice = guardianEggPrice;
	}
}
