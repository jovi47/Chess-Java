package application;

import chess.ChessMatch;

public class Program {

	public static void main(String[] args) {
		ChessMatch cM = new ChessMatch();
		UI.printBoard(cM.getPieces());
		
	}
}