package chess.domain;

import java.time.Instant;

public record Player(String playerId, String name, String avatarUrl, int wins, int losses, int draws,
		Instant lastLogin) {
	public Player onLogin(PlayerEvent.LoggedIn loggedIn) {
		return new Player(playerId, loggedIn.name(), loggedIn.avatarUrl(), wins, losses, draws,
				loggedIn.loginTime());
	}

	public Player onCreated(PlayerEvent.Created created) {
		return new Player(created.playerId(), created.name(), created.avatarUrl(), 0, 0, 0,
				created.createTime());
	}

	public Player onWon(PlayerEvent.MatchWon won) {
		return new Player(playerId, name, avatarUrl, wins + 1, losses, draws, lastLogin);
	}

	public Player onLost(PlayerEvent.MatchLost lost) {
		return new Player(playerId, name, avatarUrl, wins, losses + 1, draws, lastLogin);
	}

	public Player onDraw(PlayerEvent.MatchDraw draw) {
		return new Player(playerId, name, avatarUrl, wins, losses, draws + 1, lastLogin);
	}
}
