package chess.domain;

import java.time.Instant;

import akka.javasdk.annotations.TypeName;

public sealed interface PlayerEvent {

	@TypeName("player-loggedin")
	record LoggedIn(String playerId, Instant loginTime, String name, String avatarUrl) implements PlayerEvent {
	}

	@TypeName("player-created")
	record Created(String playerId, String name, String avatarUrl, Instant createTime)
			implements PlayerEvent {
	}

	@TypeName("match-won")
	record MatchWon(String playerId, String matchId) implements PlayerEvent {
	}

	@TypeName("match-lost")
	record MatchLost(String playerId, String matchId) implements PlayerEvent {
	}

}
