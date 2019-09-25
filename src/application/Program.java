package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		ChessMatch cM = new ChessMatch();
		List<ChessPiece> captured = new ArrayList<ChessPiece>();
		while (true) {
			try {
				UI.clearScreen();
				UI.printMatch(cM, captured);
				System.out.println();
				System.out.println("Source: ");
				ChessPosition source = UI.readChessPosition(sc);
				boolean[][] possibleMoves = cM.possibleMoves(source);
				UI.clearScreen();
				UI.printBoard(cM.getPieces(), possibleMoves);
				System.out.println();
				System.out.println("Target: ");
				ChessPosition target = UI.readChessPosition(sc);
				ChessPiece capturedPiece = cM.perfomChessMove(source, target);
				if (capturedPiece != null) {
					captured.add(capturedPiece);
				}
			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
	}
}