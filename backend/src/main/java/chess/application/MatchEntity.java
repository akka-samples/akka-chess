package chess.application;

import java.time.Instant;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import akka.javasdk.workflow.Workflow.ReadOnlyEffect;
import chess.api.MatchesApi.CreateMatchRequest;
import chess.api.MatchesApi.MatchStateResponse;
import chess.api.MatchesApi.MoveRequest;
import chess.domain.Chessboard;
import chess.domain.Match;
import chess.domain.MatchEvent;

@ComponentId("chess-match")
public class MatchEntity extends EventSourcedEntity<Match, MatchEvent> {
	private final String entityId;

	private static final Logger logger = LoggerFactory.getLogger(MatchEntity.class);

	public MatchEntity(EventSourcedEntityContext context) {
		this.entityId = context.entityId();
	}

	// NOTE: all match entities will always have a state: currentState() is never
	// null, so we need the default state to indicate an "unstarted" match
	@Override
	public Match emptyState() {
		return new Match(entityId, new Chessboard(new ArrayList<String>()), "", "", Instant.MIN);
	}

	public Effect<CommandResponse> create(CreateMatchRequest request) {
		// Not yet initialized
		if (!currentState().hasStarted()) {
			return effects()
					.persist(new MatchEvent.MatchStarted(request.matchId(), request.whiteId(),
							request.blackId(), Instant.now()))
					.thenReply(__ -> CommandResponse.accepted());
		} else {
			return effects().reply(CommandResponse.rejected("match is already started"));
		}
	}

	public Effect<CommandResponse> move(MoveRequest request) {
		if (!currentState().hasStarted()) {
			return effects().reply(CommandResponse.no_entity());
		} else {
			if (!currentState().canMove(request)) {
				return effects().reply(CommandResponse.rejected("illegal move"));
			} else {
				MatchEvent.PieceMoved moved = new MatchEvent.PieceMoved(entityId, request.agn(),
						new ArrayList<String>());
				// Finish the game if this move would finish it
				Match newMatch = currentState().onPieceMoved(moved);
				moved = new MatchEvent.PieceMoved(entityId, request.agn(),
						newMatch.board()._getUnicodePieces());
				if (newMatch.isFinished()) {
					MatchEvent finished = new MatchEvent.GameFinished(entityId,
							newMatch.getStatus(),
							Instant.now());
					ArrayList<MatchEvent> evts = new ArrayList<MatchEvent>();
					evts.add(moved);
					evts.add(finished);
					return effects().persistAll(evts).deleteEntity()
							.thenReply(__ -> CommandResponse.accepted());
				}
				return effects()
						.persist(moved).thenReply(__ -> CommandResponse.accepted());
			}
		}
	}

	public ReadOnlyEffect<String> render() {
		if (!currentState().hasStarted()) {
			return effects().error("no such match");
		} else {
			return effects().reply(currentState().render());
		}
	}

	public ReadOnlyEffect<MatchStateResponse> getMatch() {
		if (!currentState().hasStarted()) {
			return effects().error("no such match");
		} else {
			return effects().reply(currentState().getMatch());
		}
	}

	@Override
	public Match applyEvent(MatchEvent event) {
		return switch (event) {
			case MatchEvent.MatchStarted started -> currentState().onMatchStarted(started);
			case MatchEvent.PieceMoved moved -> currentState().onPieceMoved(moved);
			default -> currentState();
		};
	}

}
