package chess.domain;

public sealed interface LobbyCommand {
	public record CreatePendingMatch(String matchId, String whiteId, String joinCode) implements LobbyCommand {
	}

	public record JoinPendingMatch(String blackId, String joinCode)
			implements LobbyCommand {
	}
}
