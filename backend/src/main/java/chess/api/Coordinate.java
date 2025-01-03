package chess.api;

public record Coordinate(int row, int col) {
	public int toOffset() {
		return col + row * 8;
	}
}
