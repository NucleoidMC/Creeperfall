package io.github.redstoneparadox.creeperfall.game;

import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

public final class CreeperfallWaveBar {
	private final BossBarWidget widget;

	public CreeperfallWaveBar(GlobalWidgets widgets) {
		Component title = Component.translatable("game.creeperfall.waiting");
		this.widget = widgets.addBossBar(title, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10);
	}

	public void update(int wave, int totalWaves, int creepersRemaining, int totalCreepers) {
		Component text = Component.translatable("game.creeperfall.wave_bar", wave, totalWaves, creepersRemaining);
		float progress = (float) creepersRemaining /totalCreepers;

		this.widget.setTitle(text);
		this.widget.setProgress(progress);
	}
}
