package io.github.redstoneparadox.creeperfall.game.util;

import net.minecraft.util.FormattedCharSink;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

public class OrderedTextReader implements FormattedCharSink {
	private MutableComponent text = Component.empty();

	public Component read(FormattedCharSequence orderedText) {
		text = Component.empty();
		orderedText.accept(this);
		return text;
	}

	@Override
	public boolean accept(int index, Style style, int codePoint) {
		String string = new String(Character.toChars(codePoint));

		text.append(Component.literal(string).setStyle(style));

		return true;
	}
}
