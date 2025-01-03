package chess.application;

import java.util.List;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import chess.domain.MatchEvent;

@ComponentId("view_match_summary")
public class MatchSummaryView extends View {

	public record Matches(List<MatchSummary> matches) {
	}

	public record MatchSummary(String matchId, int moveCount, String whiteId, String blackId) {
		public MatchSummary withMoveCount(int newCount) {
			return new MatchSummary(matchId, newCount, whiteId, blackId);
		}
	}

	@Consume.FromEventSourcedEntity(MatchEntity.class)
	public static class MatchList extends TableUpdater<MatchSummary> {

		public Effect<MatchSummary> onEvent(MatchEvent event) {
			return switch (event) {
				case MatchEvent.MatchStarted started ->
					effects().updateRow(new MatchSummary(started.matchId(), 0, started.whiteId(),
							started.blackId()));
				case MatchEvent.PieceMoved moved ->
					effects().updateRow(rowState().withMoveCount(rowState().moveCount() + 1));
				case MatchEvent.GameFinished finished ->
					effects().deleteRow();
			};

		}
	}

	@Query("SELECT * as matches FROM view_match_summary")
	public QueryEffect<Matches> getMatches() {
		return queryResult();
	}
}
