package chess.domain;

import java.time.Instant;
import java.util.List;

import akka.javasdk.annotations.TypeName;

public sealed interface MatchEvent {

	@TypeName("match-started")
	record MatchStarted(String matchId, String whiteId, String blackId, Instant startTime) implements MatchEvent {
	}

	@TypeName("piece-moved")
	record PieceMoved(String matchId, String agn, List<String> pieces) implements MatchEvent {
	}

	@TypeName("game-finished")
	record GameFinished(String matchId, String whiteId, String blackId, String finalStatus, Instant finishTime)
			implements MatchEvent {
	}

}
