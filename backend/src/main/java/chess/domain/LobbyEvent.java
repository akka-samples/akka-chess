package chess.domain;

import java.time.Instant;

import akka.javasdk.annotations.TypeName;

public sealed interface LobbyEvent {
	@TypeName("pending-match-started")
	record PendingMatchCreated(String matchId, String joinCode, String whiteId, Instant started)
			implements LobbyEvent {
	}

	@TypeName("player-joined")
	record PlayerJoined(String matchId, String whiteId, String blackId) implements LobbyEvent {
	}
}
