package io.github.redstoneparadox.creeperfall.game.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class CreeperfallShopConfig {
	public static final Codec<CreeperfallShopConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("skeleton_price").forGetter(config -> config.skeletonPrice),
			Codec.INT.fieldOf("ocelot_price").forGetter(config -> config.ocelotPrice),
			Codec.INT.listOf().fieldOf("armor_upgrade_prices").forGetter(config -> config.armorUpgradePrices)
	).apply(instance, CreeperfallShopConfig::new));

	public final int skeletonPrice;
	public final int ocelotPrice;
	public final List<Integer> armorUpgradePrices;

	public CreeperfallShopConfig(int skeletonPrice, int ocelotPrice, List<Integer> armorUpgradePrices) {
		this.skeletonPrice = skeletonPrice;
		this.ocelotPrice = ocelotPrice;
		this.armorUpgradePrices = armorUpgradePrices;
	}
}
