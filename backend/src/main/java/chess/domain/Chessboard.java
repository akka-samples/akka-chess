package chess.domain;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ch.astorm.jchess.JChessGame;
import ch.astorm.jchess.core.Move;
import ch.astorm.jchess.core.Moveable;
import ch.astorm.jchess.core.Position;
import ch.astorm.jchess.core.entities.Bishop;
import ch.astorm.jchess.core.entities.King;
import ch.astorm.jchess.core.entities.Knight;
import ch.astorm.jchess.core.entities.Pawn;
import ch.astorm.jchess.core.entities.Queen;
import ch.astorm.jchess.core.entities.Rook;
import ch.astorm.jchess.io.MoveParser.InvalidMoveException;
import ch.astorm.jchess.util.UnicodePositionRenderer;
import chess.api.ChessApi.MoveRequest;

public record Chessboard(ArrayList<String> moves) {

	public Chessboard onPieceMoved(MatchEvent.PieceMoved event) {
		JChessGame game = playMoves();

		// Play the move from the event
		game.play(event.agn());

		ArrayList<String> newMoves = new ArrayList<String>(moves);
		newMoves.add(event.agn());
		return new Chessboard(newMoves);
	}

	public boolean canMove(String agn) {
		JChessGame game = JChessGame.newGame();

		for (String move : moves) {
			game.play(move);
		}

		List<Move> availableMoves = game.getAvailableMoves();
		try {
			Move tgt = game.getMove(agn);
			return availableMoves.contains(tgt);
		} catch (InvalidMoveException e) {
			return false;
		}
	}

	public String render() {
		JChessGame game = playMoves();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final String utf8 = StandardCharsets.UTF_8.name();
		try (PrintStream ps = new PrintStream(baos, true, utf8)) {
			UnicodePositionRenderer.render(ps, game.getPosition());
		} catch (UnsupportedEncodingException e) {
		}

		return baos.toString();
	}

	public boolean canMove(MoveRequest request) {
		return canMove(request.agn());
	}

	// Hide from console
	public boolean _isFinished() {
		JChessGame game = playMoves();
		return game.getStatus().isFinished();
	}

	// Hide from console
	public String _getStatus() {
		JChessGame game = playMoves();
		return game.getStatus().toString();
	}

	private JChessGame playMoves() {
		JChessGame game = JChessGame.newGame();
		for (String move : moves) {
			game.play(move);
		}
		return game;
	}

	public List<String> _getUnicodePieces() {
		JChessGame game = playMoves();
		Position position = game.getPosition();
		ArrayList<String> pieces = new ArrayList<String>();

		for (var i = 7; i >= 0; i--) {
			for (var j = 0; j < 8; j++) {
				Moveable moveable = position.get(i, j);
				if (moveable != null) {
					pieces.add(getUnicode(moveable));
				} else {
					pieces.add("");
				}
			}
		}

		return pieces;
	}

	private static String getUnicode(Moveable moveable) {
		boolean isWhite = moveable.getColor() == ch.astorm.jchess.core.Color.WHITE;
		if (moveable instanceof Rook)
			return isWhite ? "♖" : "♜";
		if (moveable instanceof Knight)
			return isWhite ? "♘" : "♞";
		if (moveable instanceof Bishop)
			return isWhite ? "♗" : "♝";
		if (moveable instanceof King)
			return isWhite ? "♔" : "♚";
		if (moveable instanceof Queen)
			return isWhite ? "♕" : "♛";
		if (moveable instanceof Pawn)
			return isWhite ? "♙" : "♟";

		return "";
	}

}
