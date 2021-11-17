package io.github.redstoneparadox.creeperfall.game.util;

import com.mojang.serialization.Codec;
import net.minecraft.predicate.NumberRange;

import java.util.Arrays;

public class Codecs {
	public static Codec<NumberRange.IntRange> INT_RANGE = Codec.INT.listOf().xmap(
			integers -> NumberRange.IntRange.between(integers.get(0), integers.get(1)),
			intRange -> Arrays.asList(intRange.getMin(), intRange.getMax())
	);
}
