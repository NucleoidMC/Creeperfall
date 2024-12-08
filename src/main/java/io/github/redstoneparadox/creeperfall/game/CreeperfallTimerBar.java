package io.github.redstoneparadox.creeperfall.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

public final class CreeperfallTimerBar {
    private final BossBarWidget widget;

    public CreeperfallTimerBar(GlobalWidgets widgets) {
        Text title = Text.translatable("game.creeperfall.waiting");
        this.widget = widgets.addBossBar(title, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10);
    }

    public void update(long ticksUntilEnd, long totalTicksUntilEnd) {
        if (ticksUntilEnd % 20 == 0) {
            this.widget.setTitle(this.getText(ticksUntilEnd));
            this.widget.setProgress((float) ticksUntilEnd / totalTicksUntilEnd);
        }
    }

    private Text getText(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / 20;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;

        return Text.translatable("game.creeperfall.time_left", minutes, formatSeconds(seconds));
    }

    private String formatSeconds(long seconds) {
        if (seconds < 10) {
            return "0" + seconds;
        }
        return String.valueOf(seconds);
    }
}
