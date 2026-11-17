package chess;

import boardgame.Board;
import chess.pieces.King;
import chess.pieces.Rook;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ChessMatchCastlingTest {
	public static void main(String[] args) throws ReflectiveOperationException {
		if (args.length == 0) {
			for (String scenario : new String[] {"short", "long", "transit", "destination", "in-check"}) {
				main(new String[] {scenario});
			}
			return;
		}
		String scenario = args[0];
		boolean longCastle = scenario.equals("long");
		char rookColumn = longCastle ? 'a' : 'h';
		char kingTarget = longCastle ? 'c' : 'g';
		char rookTarget = longCastle ? 'd' : 'f';
		ChessMatch match = new ChessMatch();
		Board board = new Board(8, 8);
		field("board").set(match, board);
		((List<?>) field("piecesOnTheBoard").get(match)).clear();
		ChessPiece king = new King(board, Color.WHITE, match);
		ChessPiece rook = new Rook(board, Color.WHITE);
		place(match, 'e', 1, king);
		place(match, rookColumn, 1, rook);
		place(match, 'e', 8, new King(board, Color.BLACK, match));

		if (scenario.equals("short") || longCastle) {
			match.perfomChessMove(new ChessPosition('e', 1), new ChessPosition(kingTarget, 1));
			require(board.piece(7, kingTarget - 'a') == king && board.piece(7, rookTarget - 'a') == rook,
					"Castling should move both pieces");
			return;
		}

		char attackingFile;
		switch (scenario) {
			case "transit": attackingFile = 'f'; break;
			case "destination": attackingFile = 'g'; break;
			case "in-check": attackingFile = 'e'; break;
			default: throw new AssertionError("Unknown case " + scenario);
		}
		place(match, attackingFile, 7, new Rook(board, Color.BLACK));
		try {
			match.perfomChessMove(new ChessPosition('e', 1), new ChessPosition(kingTarget, 1));
			throw new AssertionError("Illegal castle was accepted");
		} catch (ChessException expected) {
			require(board.piece(7, 4) == king && board.piece(7, rookColumn - 'a') == rook,
					"Illegal castle must preserve the board");
			require(king.getMoveCount() == 0 && rook.getMoveCount() == 0 && match.getTurn() == 1,
					"Illegal castle must preserve move counts and turn");
		}
	}

	private static Field field(String name) throws ReflectiveOperationException {
		Field field = ChessMatch.class.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static void place(ChessMatch match, char column, int row, ChessPiece piece)
			throws ReflectiveOperationException {
		Method method = ChessMatch.class.getDeclaredMethod("placeNewPiece", char.class, int.class, ChessPiece.class);
		method.setAccessible(true);
		method.invoke(match, column, row, piece);
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
