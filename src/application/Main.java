package application;

import java.util.InputMismatchException;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPosition;
import chess.ChessPiece;

public class Main {
    public static void main(String[] args) {
        ChessMatch chessMatch = new ChessMatch();
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                UI.clearScreen();
                UI.printBoard(chessMatch.getPieces());
                System.out.println();
                System.out.print("Source: ");
                ChessPosition source = UI.readChessPosition(sc);

                System.out.println();
                System.out.print("Target: ");
                ChessPosition target = UI.readChessPosition(sc);

                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

                System.out.println("Peça removida: " + capturedPiece);
            } catch (ChessException e) {
                System.out.print(e.getMessage() + " Press enter to exit.");
                sc.nextLine();
            }catch (InputMismatchException e) {
                System.out.print(e.getMessage() + " Press enter to exit.");
                sc.nextLine();
            }
        }

    }
}