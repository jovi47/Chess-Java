package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece {
	private Color color;
	private int moveCount;

	public ChessPiece(Board board, Color color) {
		super(board);
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public ChessPosition getChessPosition() {
		return ChessPosition.fromPosition(position);
	}

	protected boolean isThereOpponentPiece(Position position) {
		ChessPiece p = (ChessPiece) getBoard().piece(position);
		return p != null && p.getColor() != color;
	}

	protected void addOrthogonalMoves(boolean[][] moves) {
		addLineMoves(moves, -1, 0);
		addLineMoves(moves, 1, 0);
		addLineMoves(moves, 0, -1);
		addLineMoves(moves, 0, 1);
	}

	protected void addDiagonalMoves(boolean[][] moves) {
		addLineMoves(moves, -1, -1);
		addLineMoves(moves, -1, 1);
		addLineMoves(moves, 1, -1);
		addLineMoves(moves, 1, 1);
	}

	private void addLineMoves(boolean[][] moves, int rowStep, int columnStep) {
		Board board = getBoard();
		Position target = new Position(position.getRow() + rowStep, position.getColumn() + columnStep);
		while (board.positionExists(target) && !board.thereIsAPiece(target)) {
			moves[target.getRow()][target.getColumn()] = true;
			target.setValues(target.getRow() + rowStep, target.getColumn() + columnStep);
		}
		if (board.positionExists(target) && isThereOpponentPiece(target)) {
			moves[target.getRow()][target.getColumn()] = true;
		}
	}

	protected void increaseMoveCount() {
		this.moveCount++;
	}

	protected void decreaseMoveCount() {
		this.moveCount--;
	}

	public int getMoveCount() {
		return this.moveCount;
	}
}
