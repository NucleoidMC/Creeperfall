package io.github.redstoneparadox.creeperfall.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class CreeperfallShopConfig {
	public static final Codec<CreeperfallShopConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.listOf().fieldOf("armor_upgrade_prices").forGetter(config -> config.armorUpgradePrices),
			Codec.INT.listOf().fieldOf("max_arrow_upgrade_prices").forGetter(config -> config.maxArrowUpgradePrices),
			Codec.INT.fieldOf("skeleton_price").forGetter(config -> config.skeletonPrice),
			Codec.INT.fieldOf("ocelot_price").forGetter(config -> config.ocelotPrice)
	).apply(instance, CreeperfallShopConfig::new));

	public final List<Integer> armorUpgradePrices;
	public final List<Integer> maxArrowUpgradePrices;
	public final int skeletonPrice;
	public final int ocelotPrice;

	public CreeperfallShopConfig(List<Integer> armorUpgradePrices, List<Integer> maxArrowUpgradePrices, int skeletonPrice, int ocelotPrice) {
		this.armorUpgradePrices = armorUpgradePrices;
		this.maxArrowUpgradePrices = maxArrowUpgradePrices;
		this.skeletonPrice = skeletonPrice;
		this.ocelotPrice = ocelotPrice;
	}
}
