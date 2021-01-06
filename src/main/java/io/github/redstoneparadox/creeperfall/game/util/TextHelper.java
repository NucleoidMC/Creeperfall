package io.github.redstoneparadox.creeperfall.game.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class TextHelper {
	public static List<Text> wrapText(StringVisitable text, int charsPerLine) {
		String s = text.getString();

		String[] strings = WordUtils.wrap(s, charsPerLine).split("\n");
		List<Text> texts = new ArrayList<>();

		for (String string: strings) {
			texts.add(new LiteralText(string.substring(0, string.length() - 1)));
		}

		return texts;
	}
}
