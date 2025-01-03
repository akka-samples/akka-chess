package chess.domain;

public record Piece(Color color, PieceKind kind) {

	public static Piece empty() {
		return new Piece(Color.NONE, PieceKind.NONE);
	}
}
