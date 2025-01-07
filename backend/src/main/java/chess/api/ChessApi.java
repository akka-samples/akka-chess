package chess.api;

import java.time.Instant;
import java.util.List;

public sealed interface ChessApi {
	public record CreateMatchRequest(String matchId, String whiteId, String blackId) implements ChessApi {
	}

	public record MoveRequest(String agn) implements ChessApi {
	}

	public record MatchStateResponse(String matchId,
			List<String> pieces, List<String> moves, String whiteId, String blackId) implements ChessApi {
	}

	public record LoginRecord(String playerId, String name, String avatarUrl, Instant loginTime)
			implements ChessApi {
	}

	public record PlayerResponse(String name, String avatarUrl, int wins, int losses, Instant lastLogin) {
	}

}
