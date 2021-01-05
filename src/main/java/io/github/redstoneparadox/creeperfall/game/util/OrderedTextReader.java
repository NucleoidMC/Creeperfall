package io.github.redstoneparadox.creeperfall.game.util;

import net.minecraft.text.*;

public class OrderedTextReader implements CharacterVisitor {
	private MutableText text = new LiteralText("");

	public Text read(OrderedText orderedText) {
		text = new LiteralText("");
		orderedText.accept(this);
		return text;
	}

	@Override
	public boolean accept(int index, Style style, int codePoint) {
		String string = new String(Character.toChars(codePoint));

		text.append(new LiteralText(string).setStyle(style));

		return true;
	}
}
