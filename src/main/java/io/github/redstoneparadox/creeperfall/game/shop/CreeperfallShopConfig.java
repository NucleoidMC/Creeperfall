package io.github.redstoneparadox.creeperfall.game.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CreeperfallShopConfig {
	public static final Codec<CreeperfallShopConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("guardian_price").forGetter(config -> config.guardianPrice),
			Codec.INT.fieldOf("ocelot_price").forGetter(config -> config.ocelotPrice)
	).apply(instance, CreeperfallShopConfig::new));

	public final int guardianPrice;
	public final int ocelotPrice;

	public CreeperfallShopConfig(int guardianPrice, int ocelotPrice) {
		this.guardianPrice = guardianPrice;
		this.ocelotPrice = ocelotPrice;
	}
}
