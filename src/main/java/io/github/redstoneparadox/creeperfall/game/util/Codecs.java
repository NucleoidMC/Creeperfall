package io.github.redstoneparadox.creeperfall.game.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.IntRange;

import java.util.Arrays;

public class Codecs {
	public static Codec<IntRange> INT_RANGE = Codec.INT.listOf().xmap(
			integers -> new IntRange(integers.get(0), integers.get(1)),
			intRange -> Arrays.asList(intRange.getMin(), intRange.getMax())
	);
}
