package chess.domain;

import java.time.Instant;

public record Player(String playerId, String name, String avatarUrl, int wins, int losses, Instant lastLogin) {
	public Player onLogin(PlayerEvent.LoggedIn loggedIn) {
		return new Player(playerId, loggedIn.name(), loggedIn.avatarUrl(), wins, losses, loggedIn.loginTime());
	}

	public Player onCreated(PlayerEvent.Created created) {
		return new Player(created.playerId(), created.name(), created.avatarUrl(), 0, 0, created.createTime());
	}

	public Player onWon(PlayerEvent.MatchWon won) {
		return new Player(playerId, name, avatarUrl, wins + 1, losses, lastLogin);
	}

	public Player onLost(PlayerEvent.MatchLost lost) {
		return new Player(playerId, name, avatarUrl, wins, losses + 1, lastLogin);
	}
}
