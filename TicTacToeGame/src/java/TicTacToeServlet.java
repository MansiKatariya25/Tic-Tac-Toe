import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;


@WebServlet("/TicTacToe")
public class TicTacToeServlet extends HttpServlet {
    private static final int BOARD_SIZE = 3;
    private static final char EMPTY = '-';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        char[][] board = (char[][]) session.getAttribute("board");
        String action = request.getParameter("action");
        
        
        if (board == null) {
            board = initializeBoard();
            session.setAttribute("board", board);
            session.setAttribute("currentPlayer", PLAYER_X);
            session.setAttribute("gameMode", "twoPlayer");
            session.setAttribute("player1", request.getParameter("player1")); // Player 1
            session.setAttribute("player2", request.getParameter("player2")); // Player 2
        }

        // Handle actions such as reset and mode setting
        if (action != null) {
            if (action.equals("reset")) {
                board = initializeBoard();
                session.setAttribute("board", board);
                session.setAttribute("currentPlayer", PLAYER_X);
            } else if (action.equals("setMode")) {
                String mode = request.getParameter("mode");
                session.setAttribute("gameMode", mode);
                board = initializeBoard();
                session.setAttribute("board", board);
                session.setAttribute("currentPlayer", PLAYER_X);
            }
        }

        // Process a move
        int row = -1, col = -1;
        try {
            row = Integer.parseInt(request.getParameter("row"));
            col = Integer.parseInt(request.getParameter("col"));
        } catch (NumberFormatException e) {
            // No move submitted
        }

        String gameMode = (String) session.getAttribute("gameMode");
        char currentPlayer = (char) session.getAttribute("currentPlayer");

        if (row != -1 && col != -1 && board[row][col] == EMPTY) {
            board[row][col] = currentPlayer;
            if (checkWin(board, currentPlayer)) {
                session.setAttribute("winner", currentPlayer);
            } else if (isBoardFull(board)) {
                session.setAttribute("winner", 'T'); // Tie
            } else {
                currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
                session.setAttribute("currentPlayer", currentPlayer);
                
                if (gameMode.equals("singlePlayer") && currentPlayer == PLAYER_O) {
                    makeBotMove(board);
                    if (checkWin(board, PLAYER_O)) {
                        session.setAttribute("winner", PLAYER_O);
                    } else if (isBoardFull(board)) {
                        session.setAttribute("winner", 'T'); // Tie
                    } else {
                        session.setAttribute("currentPlayer", PLAYER_X);
                    }
                }
            }
        }

        // Send response
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Tic Tac Toe</h1>");
        out.println("<form method='get'>");
        out.println("<input type='radio' name='mode' value='twoPlayer' " + (gameMode.equals("twoPlayer") ? "checked" : "") + "> Two Player");
        out.println("<input type='radio' name='mode' value='singlePlayer' " + (gameMode.equals("singlePlayer") ? "checked" : "") + "> Single Player");
        out.println("<input type='submit' name='action' value='setMode'>");
        out.println("</form>");
        
        renderBoard(out, board);
        
        Character winner = (Character) session.getAttribute("winner");
        if (winner != null) {
            if (winner == 'T') {
                out.println("<p>It's a tie!</p>");
            } else {
                out.println("<p>Player " + winner + " wins!</p>");
            }
            out.println("<form method='get'><input type='submit' name='action' value='reset'></form>");
        }
        
        out.println("</body></html>");
    }

    private char[][] initializeBoard() {
        char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], EMPTY);
        }
        return board;
    }

    private void renderBoard(PrintWriter out, char[][] board) {
        out.println("<table border='1' style='border-collapse: collapse;'>");
        for (int row = 0; row < BOARD_SIZE; row++) {
            out.println("<tr>");
            for (int col = 0; col < BOARD_SIZE; col++) {
                out.println("<td style='width: 100px; height: 100px; text-align: center; cursor: pointer;' onclick=\"location.href='?row=" + row + "&col=" + col + "'\">" + board[row][col] + "</td>");
            }
            out.println("</tr>");
        }
        out.println("</table>");
    }

    private boolean checkWin(char[][] board, char player) {
        // Check rows
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            return true;
        }
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            return true;
        }

        return false;
    }

    private boolean isBoardFull(char[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private void makeBotMove(char[][] board) {
        Random rand = new Random();
        int row, col;
        do {
            row = rand.nextInt(BOARD_SIZE);
            col = rand.nextInt(BOARD_SIZE);
        } while (board[row][col] != EMPTY);
        board[row][col] = PLAYER_O;
    }
}