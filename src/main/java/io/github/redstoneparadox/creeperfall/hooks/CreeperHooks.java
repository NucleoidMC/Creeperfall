package io.github.redstoneparadox.creeperfall.hooks;

public interface CreeperHooks {
	/**
	 * <p>Used to enable behaviors specific to Creeperfall that have
	 * to be added via mixin</p>
	 *
	 * @param isCreeperfallCreeper Whether this is a Creeperfall creeper
	 */
	void setCreeperfallCreeper(boolean isCreeperfallCreeper);

	boolean isCreeperfallCreeper();
}
