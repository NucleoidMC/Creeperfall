package io.github.redstoneparadox.creeperfall.game.util;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class TextHelper {
	public static List<Component> wrapText(FormattedText text, int charsPerLine) {
		String s = text.getString();

		String[] strings = WordUtils.wrap(s, charsPerLine).split("\n");
		List<Component> texts = new ArrayList<>();

		for (String string: strings) {
			texts.add(Component.literal(string.substring(0, string.length() - 1)));
		}

		return texts;
	}
}
