package chess.api;

import java.util.List;

public sealed interface MatchesApi {
	public record CreateMatchRequest(String matchId, String whiteId, String blackId) implements MatchesApi {
	}

	public record MoveRequest(String agn) implements MatchesApi {
	}

	public record MatchStateResponse(String matchId,
			List<String> pieces, List<String> moves, String whiteId, String blackId) implements MatchesApi {
	}

}
