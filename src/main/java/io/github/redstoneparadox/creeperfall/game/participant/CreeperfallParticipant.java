package io.github.redstoneparadox.creeperfall.game.participant;

import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class CreeperfallParticipant {
    private final PlayerRef player;
    private final GameSpace gameSpace;

	public CreeperfallParticipant(PlayerRef player, GameSpace gameSpace) {
		this.player = player;
		this.gameSpace = gameSpace;
	}

	public PlayerRef getPlayer() {
		return player;
	}

	public GameSpace getGameSpace() {
		return gameSpace;
	}
}
