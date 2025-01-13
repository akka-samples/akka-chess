package chess.domain;

import java.time.Instant;

import chess.api.ChessApi.MatchStateResponse;
import chess.api.ChessApi.MoveRequest;

public record Match(String matchId, Chessboard board, String whiteId, String blackId, Instant matchStart) {

	public Match onMatchStarted(MatchEvent.MatchStarted event) {
		return new Match(matchId, board, event.whiteId(), event.blackId(), event.startTime());
	}

	public Match onPieceMoved(MatchEvent.PieceMoved event) {
		return new Match(matchId, board.onPieceMoved(event), whiteId, blackId, matchStart);
	}

	public boolean canMove(String playerId, MoveRequest request) {
		return (playerId == getCurrentPlayerId() && board.canMove(request));
	}

	public String render() {
		return board.render();
	}

	public String getCurrentPlayerId() {
		return board.getColorOnMove() == "WHITE" ? whiteId : blackId;
	}

	public MatchStateResponse getMatch() {
		String currentId = getCurrentPlayerId();
		return new MatchStateResponse(matchId, board._getUnicodePieces(), board.moves(), whiteId, blackId,
				board._getStatus(),
				currentId);
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
