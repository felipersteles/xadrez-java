package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import board.Board;
import board.Piece;
import board.Position;
import chess.pieces.King;
import chess.pieces.Rook;

// coracao do nosso
// sistema de xadrez
public class ChessMatch {
    // tabuleiro do jogo
    private Board board;

    // variaveis da jogabilidade
    private int turn;
    private Color currentPlayer;
    private boolean check; // por padrão ela é falsa
    private boolean checkMate;

    // controle das peças comidas
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8, 8);
        currentPlayer = Color.WHITE;
        turn = 1;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
		return checkMate;
	}

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    // as peças do tipo Piece não são retornadas e sim
    // uma variacao delas, isto acontece para evitar erros
    // durante o desenvolvimento
    public ChessPiece[][] getPieces() {
        ChessPiece[][] matrix = new ChessPiece[board.getRows()][board.getColumns()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                matrix[i][j] = (ChessPiece) board.piece(i, j);
            }
        }

        return matrix;
    }

    // metodo que faz uma
    // jogada de xadrez
    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();

        validateSourcePosition(source);
        validateTargetPosition(source, target);

        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("Não podes se colocar em check...");
        }

        check = (testCheck((opponent(currentPlayer)))) ? true : false;

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        return (ChessPiece) capturedPiece;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);

        return board.piece(position).possibleMoves();
    }

    private Piece makeMove(Position sourcePosition, Position targetPosition) {
        Piece capturingPiece = board.removePiece(sourcePosition);
        Piece capturedPiece = board.removePiece(targetPosition);
        board.placePiece(capturingPiece, targetPosition);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        return capturedPiece;
    }

    // desfazendo o metodo acima
    private void undoMove(Position sourcePosition, Position targetPosition, Piece capturedPiece) {
        Piece piece = board.removePiece(targetPosition);
        board.placePiece(piece, sourcePosition);

        // voltando a peça para o tabuleiro
        if (capturedPiece != null) {
            board.placePiece(capturedPiece, targetPosition);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("Não tem peça na posição de origem.");
        }

        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
            throw new ChessException("A peça escolhida não é do seu time.");
        }

        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("Não existem movimentos possiveis para a peça escolhida.");
        }
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("A peça escolhida nao pode se mover para a posição selecionada.");
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    // metodo que busca a peca REI
    // finding the King
    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(piece -> ((ChessPiece) piece).getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }

        throw new IllegalStateException("Não existe o rei da equipe " + color);
    }

    // compara a matrix de movimentos
    // de todas as peças do oponente
    // com a pos do rei
    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentList = piecesOnTheBoard.stream()
                .filter(piece -> ((ChessPiece) piece).getColor() == opponent(color))
                .collect(Collectors.toList());

        for (Piece p : opponentList) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()])
                return true;
        }

        return false;
    }

    private boolean testCheckMate(Color color) {
        if (!testCheck(color)) {
            return false;
        }

        List<Piece> pieces = piecesOnTheBoard.stream()
                .filter(piece -> ((ChessPiece) piece).getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : pieces) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < mat.length; i++) {
                for (int j = 0; j < mat.length; j++) {
                    if (mat[i][j]) {
                        // testar se o movimento possivel tira do check
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);

                        Piece capturedPiece = makeMove(source, target);

                        boolean testCheck = testCheck(color);

                        // desfazendo o movimento
                        // utilizado apenas para testar
                        undoMove(source, target, capturedPiece);

                        if (!testCheck)
                            return false;
                    }
                }
            }
        }

        return true;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup() {
        placeNewPiece('h', 7, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE));

        placeNewPiece('b', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 8, new King(board, Color.BLACK));
    }
}
