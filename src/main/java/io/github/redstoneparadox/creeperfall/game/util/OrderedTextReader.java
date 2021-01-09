package io.github.redstoneparadox.creeperfall.game.util;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

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
