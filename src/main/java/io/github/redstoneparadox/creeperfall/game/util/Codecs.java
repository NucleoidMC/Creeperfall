package io.github.redstoneparadox.creeperfall.game.util;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.predicates.MinMaxBounds;

import java.util.Arrays;

public class Codecs {
	public static Codec<MinMaxBounds.Ints> INT_RANGE = Codec.INT.listOf().xmap(
			integers -> MinMaxBounds.Ints.between(integers.get(0), integers.get(1)),
			intRange -> Arrays.asList(intRange.min().orElse(0), intRange.max().orElse(1))
	);
}
