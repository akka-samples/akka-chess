package chess.api;

import java.time.Instant;
import java.util.List;

public sealed interface ChessApi {
	public record CreateMatchRequest(String matchId, String whiteId, String blackId) implements ChessApi {
	}

	public record JoinLobbyMatchRequest(String joinCode, String blackId) implements ChessApi {
	}

	public record CreateLobbyMatchRequest(String matchId, String whiteId) implements ChessApi {
	}

	public record MoveRequest(String agn) implements ChessApi {
	}

	public record MatchStateResponse(String matchId,
			List<String> pieces, List<String> moves, String whiteId, String blackId, String status,
			String currentPlayerId)
			implements ChessApi {
	}

	public record LoginRecord(String playerId, String name, String avatarUrl, Instant loginTime)
			implements ChessApi {
	}

	public record PlayerResponse(String name, String avatarUrl, int wins, int losses, int draws,
			Instant lastLogin) {
	}

	public record PendingMatch(String matchId, String whiteId, String joinCode, Instant started) {
	}

	public record LobbyMatches(List<PendingMatch> matches) {
	}

}
