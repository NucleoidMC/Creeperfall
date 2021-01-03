package io.github.redstoneparadox.creeperfall.game.util;

public class Timer {
	private final long time;
	private long timeRemaining;
	private boolean repeat;
	private Runnable onFire;

	private Timer(long time, boolean repeat, Runnable onFire) {
		this.time = time;
		this.timeRemaining = time;
		this.repeat = repeat;
		this.onFire = onFire;
	}

	public static Timer create(long time, Runnable onFire) {
		return new Timer(time, false, onFire);
	}

	public static Timer createRepeating(long time, Runnable onFire) {
		return new Timer(time, true, onFire);
	}

	public void tick() {
		timeRemaining -= 1;

		if (timeRemaining <= 0) {
			onFire.run();

			if (repeat) {
				timeRemaining = time;
			}
		}
	}
}
