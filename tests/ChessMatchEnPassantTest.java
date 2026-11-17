package chess;

import boardgame.Board;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Rook;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ChessMatchEnPassantTest {
	public static void main(String[] args) throws ReflectiveOperationException {
		if (args.length == 0) {
			for (String scenario : new String[] {
					"white-right", "white-left", "black-right", "black-left", "rollback", "check", "ordinary"
			}) {
				main(new String[] {scenario});
			}
			return;
		}
		switch (args[0]) {
			case "white-right":
				capture("a5", "b6", "b5", "a2a4", "h7h6", "a4a5", "b7b5");
				break;
			case "white-left":
				capture("e5", "d6", "d5", "e2e4", "h7h6", "e4e5", "d7d5");
				break;
			case "black-right":
				capture("a4", "b3", "b4", "h2h3", "a7a5", "h3h4", "a5a4", "b2b4");
				break;
			case "black-left":
				capture("e4", "d3", "d4", "h2h3", "e7e5", "h3h4", "e5e4", "d2d4");
				break;
			case "rollback":
				rollback();
				break;
			case "check":
				escapeCheck();
				break;
			case "ordinary":
				capture("e4", "d5", "d5", "e2e4", "d7d5");
				break;
			default:
				throw new AssertionError("Unknown case " + args[0]);
		}
	}

	private static void capture(String source, String target, String victim, String... setup) {
		ChessMatch match = new ChessMatch();
		for (String turn : setup) move(match, turn);
		ChessPiece mover = piece(match, source);
		ChessPiece pawn = piece(match, victim);
		require(move(match, source + target) == pawn, "Wrong captured piece");
		require(piece(match, target) == mover && (target.equals(victim) || piece(match, victim) == null), "Wrong board after capture");
		require(match.getEnPassantVulnerable() == null, "Vulnerability must expire after capture");
	}

	private static void rollback() throws ReflectiveOperationException {
		ChessMatch match = emptyMatch(Color.BLACK);
		Board board = (Board) field("board").get(match);
		place(match, "e1", new King(board, Color.WHITE, match));
		King blackKing = new King(board, Color.BLACK, match);
		blackKing.increaseMoveCount();
		place(match, "a8", blackKing);
		place(match, "e8", new Rook(board, Color.BLACK));
		place(match, "e5", new Pawn(board, Color.WHITE, match));
		place(match, "d7", new Pawn(board, Color.BLACK, match));
		move(match, "d7d5");
		ChessPiece captured = piece(match, "d5");
		ChessPiece mover = piece(match, "e5");
		try {
			move(match, "e5d6");
			throw new AssertionError("Capture must be rejected for exposing the king");
		} catch (ChessException expected) {
			require(piece(match, "e5") == mover && piece(match, "d5") == captured
					&& piece(match, "d6") == null, "Rejected capture corrupted the board");
			require(match.getEnPassantVulnerable() == captured, "Rejected move expired the vulnerable pawn");
		}
	}

	private static void escapeCheck() throws ReflectiveOperationException {
		ChessMatch match = emptyMatch(Color.BLACK);
		Board board = (Board) field("board").get(match);
		place(match, "e4", new King(board, Color.WHITE, match));
		place(match, "e6", new King(board, Color.BLACK, match));
		place(match, "c2", new Knight(board, Color.BLACK));
		for (String square : new String[] {"d3", "e3", "f3", "f4", "e5"}) {
			place(match, square, new Pawn(board, Color.WHITE, match));
		}
		place(match, "d7", new Pawn(board, Color.BLACK, match));
		move(match, "d7d5");
		require(match.getCheck() && !match.getCheckMate() && match.getCurrentPlayer() == Color.WHITE,
				"En passant is the only escape from check");
		require(piece(match, "d5") != null && piece(match, "d6") == null,
				"Checkmate search moved a pawn");
		move(match, "e5d6");
	}

	private static ChessMatch emptyMatch(Color player) throws ReflectiveOperationException {
		ChessMatch match = new ChessMatch();
		field("board").set(match, new Board(8, 8));
		((List<?>) field("piecesOnTheBoard").get(match)).clear();
		field("currentPlayer").set(match, player);
		return match;
	}

	private static Field field(String name) throws ReflectiveOperationException {
		Field field = ChessMatch.class.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static void place(ChessMatch match, String square, ChessPiece piece)
			throws ReflectiveOperationException {
		Method method = ChessMatch.class.getDeclaredMethod("placeNewPiece", char.class, int.class, ChessPiece.class);
		method.setAccessible(true);
		method.invoke(match, square.charAt(0), square.charAt(1) - '0', piece);
	}

	private static ChessPiece move(ChessMatch match, String move) {
		return match.perfomChessMove(position(move.substring(0, 2)), position(move.substring(2, 4)));
	}

	private static ChessPiece piece(ChessMatch match, String square) {
		Position position = position(square).toPosition();
		return match.getPieces()[position.getRow()][position.getColumn()];
	}

	private static ChessPosition position(String square) {
		return new ChessPosition(square.charAt(0), square.charAt(1) - '0');
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
