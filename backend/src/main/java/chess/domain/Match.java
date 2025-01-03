package chess.domain;

import java.time.Instant;

import chess.api.MatchesApi.MatchStateResponse;
import chess.api.MatchesApi.MoveRequest;

public record Match(String matchId, Chessboard board, String whiteId, String blackId, Instant matchStart) {

	public Match onMatchStarted(MatchEvent.MatchStarted event) {
		return new Match(matchId, board, event.whiteId(), event.blackId(), event.startTime());
	}

	public Match onPieceMoved(MatchEvent.PieceMoved event) {
		return new Match(matchId, board.onPieceMoved(event), whiteId, blackId, matchStart);
	}

	public boolean canMove(MoveRequest request) {
		return board.canMove(request);
	}

	public String render() {
		return board.render();
	}

	public MatchStateResponse getMatch() {
		return new MatchStateResponse(matchId, board._getUnicodePieces(), board.moves(), whiteId, blackId);
	}

	public boolean hasStarted() {
		return matchStart != Instant.MIN;
	}

	public boolean isFinished() {
		return board._isFinished();
	}

	public String getStatus() {
		return board._getStatus();
	}
}
