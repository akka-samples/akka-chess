package chess.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import chess.domain.MatchEvent;

@ComponentId("view_match_archive")
public class MatchArchiveView extends View {
	public record MatchArchives(List<MatchArchive> matches) {
	}

	public record MatchArchive(String matchId, List<String> moves, String whiteId, String blackId,
			long started, long finished) {

		public MatchArchive addMove(String agn) {
			ArrayList<String> newMoves = new ArrayList<String>(moves);
			newMoves.add(agn);
			return new MatchArchive(matchId, newMoves, whiteId, blackId, started, finished);
		}

		public MatchArchive finish() {
			return new MatchArchive(matchId, moves, whiteId, blackId, started,
					Instant.now().toEpochMilli());
		}
	}

	@Consume.FromEventSourcedEntity(MatchEntity.class)
	public static class MatchArchiveList extends TableUpdater<MatchArchive> {

		public Effect<MatchArchive> onEvent(MatchEvent event) {
			return switch (event) {
				case MatchEvent.MatchStarted started -> startMatch(started);
				case MatchEvent.PieceMoved moved -> updateMatch(moved);
				case MatchEvent.GameFinished finished -> finishMatch(finished);
			};
		}

		private Effect<MatchArchive> startMatch(MatchEvent.MatchStarted started) {
			return effects().updateRow(
					new MatchArchive(started.matchId(), new ArrayList<String>(), started.whiteId(),
							started.blackId(), Instant.now().toEpochMilli(), 0));
		}

		private Effect<MatchArchive> updateMatch(MatchEvent.PieceMoved moved) {
			return effects().updateRow(rowState().addMove(moved.agn()));
		}

		private Effect<MatchArchive> finishMatch(MatchEvent.GameFinished finished) {
			return effects().updateRow(rowState().finish());
		}

	}

	@Query("SELECT * as matches FROM view_match_archive WHERE blackId = :playerId OR whiteId = :playerId")
	public QueryEffect<MatchArchives> getMatchesByPlayer(String playerId) {
		return queryResult();
	}

}
