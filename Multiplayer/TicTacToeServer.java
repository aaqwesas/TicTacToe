// TicTacToeServer.java
import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * The {@code TicTacToeServer} class manages a multiplayer Tic-Tac-Toe game by facilitating
 * communication between two connected clients. It handles game state management, score tracking,
 * and ensures coordinated gameplay, including game restarts based on players' decisions.
 *
 * <p>
 * Key Functionalities:
 * </p>
 * <ul>
 *     <li>Accepts incoming client connections to initialize a game session.</li>
 *     <li>Manages the game board and determines game outcomes (win, draw).</li>
 *     <li>Tracks and updates cumulative scores for both players.</li>
 *     <li>Handles player requests to restart the game or quit.</li>
 *     <li>Ensures graceful termination of game sessions upon player disconnections or quits.</li>
 * </ul>
 *
 * <p>
 * Communication Protocol:
 * </p>
 * <ul>
 *     <li><b>START &lt;Mark&gt;:</b> Informs the client of their assigned mark ('X' or 'O').</li>
 *     <li><b>PLAYER1 &lt;Name&gt;, PLAYER2 &lt;Name&gt;:</b> Sends the names of both players to each client.</li>
 *     <li><b>BOARD &lt;State&gt;:</b> Updates the game board on clients with the current state.</li>
 *     <li><b>SCORE &lt;Player1Wins&gt; &lt;Player2Wins&gt; &lt;Draws&gt;:</b> Sends cumulative scores.</li>
 *     <li><b>MESSAGE &lt;Text&gt;:</b> Sends informative messages to clients.</li>
 *     <li><b>END &lt;Message&gt;:</b> Notifies clients of game conclusions and prompts for restarts.</li>
 *     <li><b>Quit &lt;Message&gt;:</b> Informs the remaining client that the opponent has left.</li>
 *     <li><b>INVALID &lt;Error&gt;:</b> Notifies clients of invalid actions or inputs.</li>
 *     <li><b>TITLE &lt;Title&gt;:</b> Sets the client's window title.</li>
 * </ul>
 * @author
 * @version 1.2
 * @since 2024-04-28
 */
public class TicTacToeServer {
    /**
     * The port number on which the server listens for incoming client connections.
     */
    private static final int PORT = 7000;

    /**
     * Handler for Player 1.
     */
    private PlayerHandler player1;

    /**
     * Handler for Player 2.
     */
    private PlayerHandler player2;

    /**
     * Represents the Tic-Tac-Toe game board as an array of 9 strings.
     * Each index corresponds to a cell on the 3x3 board.
     * An empty string represents an unoccupied cell.
     */
    private String[] board = new String[9];

    /**
     * Flag indicating whether the current game has ended.
     * Used to prevent further moves and manage game state transitions.
     */
    private boolean gameEnded = false;

    /**
     * Cumulative score for Player 1 (number of wins).
     */
    private int player1Wins = 0;

    /**
     * Cumulative score for Player 2 (number of wins).
     */
    private int player2Wins = 0;

    /**
     * Cumulative count of drawn games.
     */
    private int draws = 0;

    /**
     * Flag indicating whether Player 1 has requested to restart the game after a game ends.
     */
    private boolean player1WantsRestart = false;

    /**
     * Flag indicating whether Player 2 has requested to restart the game after a game ends.
     */
    private boolean player2WantsRestart = false;

    /**
     * Describes the outcome of the current game (e.g., win, draw).
     * Useful for logging and informing clients.
     */
    private String gameOutcome = "";

    /**
     * Constructs a {@code TicTacToeServer} instance, initializes the game,
     * and waits for client connections to start the game session.
     *
     * <p>
     * The server performs the following steps:
     * </p>
     * <ol>
     *     <li>Initializes the game board to empty.</li>
     *     <li>Starts a server socket on the specified port.</li>
     *     <li>Accepts connections from two clients, initializing Player 1 and Player 2.</li>
     *     <li>Establishes communication between the server and each player.</li>
     *     <li>Sends initial game setup messages to both clients.</li>
     *     <li>Broadcasts the initial game board to both clients.</li>
     * </ol>
     */
    public TicTacToeServer() {
        // Initialize the game board with empty strings
        Arrays.fill(board, "");

        // Attempt to start the server and accept client connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Tic-Tac-Toe Server is running...");

            // Accept Player 1
            System.out.println("Waiting for Player 1 to connect...");
            Socket socket1 = serverSocket.accept();
            System.out.println("Player 1 connected.");
            player1 = new PlayerHandler(socket1, "X", "Player 1"); // Assign mark 'X' to Player 1

            // Accept Player 2
            System.out.println("Waiting for Player 2 to connect...");
            Socket socket2 = serverSocket.accept();
            System.out.println("Player 2 connected.");
            player2 = new PlayerHandler(socket2, "O", "Player 2"); // Assign mark 'O' to Player 2

            // Set opponents for each player
            player1.setOpponent(player2);
            player2.setOpponent(player1);

            // Start communication threads for both players
            player1.start();
            player2.start();

            // Notify players of their assigned marks
            player1.sendMessage("START X");
            player2.sendMessage("START O");

            // Send player names to both clients for score tracking
            player1.sendMessage("PLAYER1 " + player1.getPlayerName());
            player1.sendMessage("PLAYER2 " + player2.getPlayerName());

            player2.sendMessage("PLAYER1 " + player1.getPlayerName());
            player2.sendMessage("PLAYER2 " + player2.getPlayerName());

            // Send initial cumulative scores to both players
            sendScores();

            // Send welcome messages to both players
            player1.sendMessage("MESSAGE Welcome " + player1.getPlayerName() + "! Waiting for Player 2 to make a move.");
            player2.sendMessage("MESSAGE Welcome " + player2.getPlayerName() + "! Waiting for Player 1 to make a move.");

            // Broadcast the initial empty game board to both players
            broadcastBoard();

        } catch (IOException e) {
            System.err.println("Server encountered an error during initialization.");
            e.printStackTrace();
        }
    }

    /**
     * Sends the current cumulative scores to both connected players.
     *
     * <p>
     * The score message follows the format: "SCORE &lt;Player1Wins&gt; &lt;Player2Wins&gt; &lt;Draws&gt;"
     * </p>
     */
    private synchronized void sendScores() {
        String scoreMessage = "SCORE " + player1Wins + " " + player2Wins + " " + draws;
        if (player1 != null) {
            player1.sendMessage(scoreMessage);
        }
        if (player2 != null) {
            player2.sendMessage(scoreMessage);
        }
    }

    /**
     * Checks whether the specified player has achieved a winning condition on the game board.
     *
     * <p>
     * A player wins if they have three of their marks in a horizontal, vertical, or diagonal line.
     * </p>
     *
     * @param mark The player's mark ("X" or "O").
     * @return {@code true} if the player has won; {@code false} otherwise.
     */
    private synchronized boolean checkWinner(String mark) {
        // Define all possible winning conditions (rows, columns, diagonals)
        int[][] winConditions = {
            {0, 1, 2}, // Top row
            {3, 4, 5}, // Middle row
            {6, 7, 8}, // Bottom row
            {0, 3, 6}, // Left column
            {1, 4, 7}, // Middle column
            {2, 5, 8}, // Right column
            {0, 4, 8}, // Main diagonal
            {2, 4, 6}  // Anti-diagonal
        };

        // Iterate through all winning conditions to check for a match
        for (int[] condition : winConditions) {
            if (board[condition[0]].equals(mark) &&
                board[condition[1]].equals(mark) &&
                board[condition[2]].equals(mark)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the game board is completely occupied with no empty cells.
     *
     * @return {@code true} if the board is full; {@code false} otherwise.
     */
    private synchronized boolean isBoardFull() {
        for (String cell : board) {
            if (cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Broadcasts the current state of the game board to both connected players.
     *
     * <p>
     * The board state is sent in a comma-separated format where each cell contains either
     * "X", "O", or "-" to represent an empty cell. For example: "X,O,-,X,-,O,-,--,X"
     * </p>
     */
    private synchronized void broadcastBoard() {
        StringBuilder sb = new StringBuilder();
        for (String cell : board) {
            sb.append(cell.isEmpty() ? "-" : cell).append(",");
        }
        // Remove the trailing comma if present
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        String boardState = sb.toString();

        // Send the board state to Player 1
        if (player1 != null) {
            player1.sendMessage("BOARD " + boardState);
        }

        // Send the board state to Player 2
        if (player2 != null) {
            player2.sendMessage("BOARD " + boardState);
        }
    }

    /**
     * Concludes the current game by sending appropriate messages to both players,
     * updating scores, and managing game state for possible restarts or termination.
     *
     * <p>
     * This method handles two scenarios:
     * <ul>
     *     <li>Normal game conclusion (win or draw).</li>
     *     <li>Game termination due to a player quitting or disconnecting.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Depending on the scenario, it updates the scores, sends relevant messages to the players,
     * and sets the {@code gameEnded} flag to prevent further gameplay until a restart decision is made.
     * </p>
     *
     * @param message  A descriptive message about the game outcome (e.g., "Player X wins!").
     * @param quitter  The {@code PlayerHandler} of the player who initiated the game termination.
     *                 If {@code null}, it indicates a normal game conclusion.
     */
    private synchronized void endGame(String message, PlayerHandler quitter) {
        if (!gameEnded) {
            gameEnded = true;

            // Determine the outcome of the game
            if (quitter == null) {
                // Normal game end (win or draw)
                gameOutcome = message;
            } else {
                // Game end due to a player quitting or disconnecting
                gameOutcome = message;
            }

            if (quitter != null) {
                // A player has quit or declined to restart
                if (quitter == player1) {
                    // Inform the quitting player about the game end
                    player1.sendMessage("END " + message);
                    // Notify the remaining player that their opponent has quit
                    player2.sendMessage("Quit " + message);
                    // Update scores: Opponent (Player 2) wins by default
                    player2Wins++;
                } else if (quitter == player2) {
                    // Inform the quitting player about the game end
                    player2.sendMessage("END " + message);
                    // Notify the remaining player that their opponent has quit
                    player1.sendMessage("Quit " + message);
                    // Update scores: Opponent (Player 1) wins by default
                    player1Wins++;
                }
            } else {
                // Normal game end (win or draw)
                if (message.contains(player1.getPlayerName())) {
                    // Player 1 wins
                    player1.sendMessage("END " + message);       // Notify Player 1 of their win
                    player2.sendMessage("END " + "You lose."); // Notify Player 2 of their loss
                    player1Wins++;
                } else if (message.contains(player2.getPlayerName())) {
                    // Player 2 wins
                    player2.sendMessage("END " + message);       // Notify Player 2 of their win
                    player1.sendMessage("END " + "You lose."); // Notify Player 1 of their loss
                    player2Wins++;
                } else {
                    // Draw
                    player1.sendMessage("END " + message); // Notify Player 1 of the draw
                    player2.sendMessage("END " + message); // Notify Player 2 of the draw
                    draws++;
                }
            }

            // Update and send the latest scores to both players
            sendScores();

            System.out.println("Game Ended: " + message);
            // Note: Resetting of gameEnded and board will occur after handling restart decisions
        }
    }

    /**
     * Handles the restart decision process based on players' requests to continue playing.
     * The game will only restart if both players agree; otherwise, the game will terminate.
     *
     * @param player The {@code PlayerHandler} of the player who has requested a restart.
     */
    private synchronized void handleRestartDecision(PlayerHandler player) {
        if (player == player1) {
            player1WantsRestart = true; // Mark Player 1's desire to restart
        } else if (player == player2) {
            player2WantsRestart = true; // Mark Player 2's desire to restart
        }

        // Check if both players have agreed to restart
        if (player1WantsRestart && player2WantsRestart) {
            // Both players want to restart; reset the game state
            player1WantsRestart = false;
            player2WantsRestart = false;
            gameEnded = false;
            Arrays.fill(board, ""); // Clear the game board

            // Broadcast the cleared game board to both players
            broadcastBoard();

            // Send updated scores to both players
            sendScores();

            // Notify both players that the game has restarted
            player1.sendMessage("MESSAGE The game has been restarted!");
            player2.sendMessage("MESSAGE The game has been restarted!");

            // Inform players whose turn it is based on their marks
            player1.sendMessage("MESSAGE " + (player1.getMark().equals("X") ? "Your turn." : "Waiting for opponent's move."));
            player2.sendMessage("MESSAGE " + (player2.getMark().equals("O") ? "Your turn." : "Waiting for opponent's move."));
        }
    }

    /**
     * Terminates the current game session due to one player's decision to quit or due to unexpected events.
     * Notifies both players accordingly and updates the cumulative scores.
     *
     * @param message  A descriptive message about the termination reason (e.g., "Player X has quit the game.").
     * @param quitter  The {@code PlayerHandler} of the player who initiated the termination.
     */
    private synchronized void terminateGame(String message, PlayerHandler quitter) {
        if (quitter == player1) {
            // Inform the quitting player about the termination
            player1.sendMessage("END " + message);
            // Notify the remaining player that their opponent has quit
            player2.sendMessage("Quit " + message);
            // Update scores: Opponent (Player 2) wins by default
            player2Wins++;
        } else if (quitter == player2) {
            // Inform the quitting player about the termination
            player2.sendMessage("END " + message);
            // Notify the remaining player that their opponent has quit
            player1.sendMessage("Quit " + message);
            // Update scores: Opponent (Player 1) wins by default
            player1Wins++;
        }

        // Update and send the latest scores to both players
        sendScores();

        System.out.println("Game Terminated: " + message);
    }

    /**
     * The {@code PlayerHandler} class manages the communication with an individual client.
     * It handles incoming messages from the client, processes game actions, and sends responses.
     *
     * <p>
     * Each {@code PlayerHandler} runs on its own thread to allow simultaneous interactions with both players.
     * </p>
     */
    private class PlayerHandler extends Thread {
        /**
         * The socket associated with the connected client.
         */
        private Socket socket;

        /**
         * Reader to receive messages from the client.
         */
        private BufferedReader in;

        /**
         * Writer to send messages to the client.
         */
        private PrintWriter out;

        /**
         * Reference to the opponent's {@code PlayerHandler}.
         */
        private PlayerHandler opponent;

        /**
         * The mark assigned to the player ("X" or "O").
         */
        private String mark;

        /**
         * The player's chosen name.
         */
        private String playerName;

        /**
         * Constructs a {@code PlayerHandler} for a connected client, initializing communication streams
         * and prompting for the player's name.
         *
         * @param socket      The client's socket connection.
         * @param mark        The player's assigned mark ("X" or "O").
         * @param defaultName The default name assigned to the player if they do not provide one.
         */
        public PlayerHandler(Socket socket, String mark, String defaultName) {
            this.socket = socket;
            this.mark = mark;
            try {
                // Initialize input and output streams for communication
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message and prompt for the player's name
                sendMessage("WELCOME " + defaultName);
                sendMessage("MESSAGE Please enter your name:");

                // Read the player's name from the input stream
                String nameMessage = in.readLine();
                System.out.println(mark + " received message: " + nameMessage); // Debugging Line

                if (nameMessage != null && nameMessage.startsWith("NAME")) {
                    // Parse the player's name from the message
                    String[] tokens = nameMessage.split(" ", 2);
                    if (tokens.length >= 2 && !tokens[1].trim().isEmpty()) {
                        playerName = tokens[1].trim();
                    } else {
                        // Assign the default name if the provided name is invalid
                        playerName = defaultName;
                        sendMessage("INVALID Invalid name format. Using default name.");
                    }
                } else {
                    // Assign the default name if no name was provided
                    playerName = defaultName;
                    sendMessage("INVALID Name not provided. Using default name.");
                }

                // Set the client's window title based on their name
                setTitle();

                // Send a welcome message with the player's name
                sendMessage("MESSAGE WELCOME " + playerName);
            } catch (IOException e) {
                System.err.println("Player disconnected during setup.");
            }
        }

        /**
         * Retrieves the player's chosen name.
         *
         * @return The player's name.
         */
        public String getPlayerName() {
            return playerName;
        }

        /**
         * Retrieves the player's assigned mark.
         *
         * @return The player's mark ("X" or "O").
         */
        public String getMark() {
            return mark;
        }

        /**
         * Sets the opponent for this player.
         *
         * @param opponent The {@code PlayerHandler} of the opposing player.
         */
        public void setOpponent(PlayerHandler opponent) {
            this.opponent = opponent;
        }

        /**
         * Sends a message to the client.
         *
         * @param message The message to send.
         */
        public void sendMessage(String message) {
            out.println(message);
        }

        /**
         * Sets the client's window title based on their name.
         *
         * <p>
         * The title follows the format: "Tic Tac Toe - &lt;PlayerName&gt;"
         * </p>
         */
        private void setTitle() {
            sendMessage("TITLE Tic Tac Toe - " + playerName);
        }

        /**
         * The main execution method for the {@code PlayerHandler} thread.
         *
         * <p>
         * This method continuously listens for incoming messages from the client,
         * processes actions such as moves, restarts, and quits, and manages game state accordingly.
         * </p>
         */
        @Override
        public void run() {
            try {
                String input;
                // Continuously listen for messages from the client
                while ((input = in.readLine()) != null) {
                    System.out.println(playerName + " sent: " + input); // Debugging Line

                    if (input.startsWith("MOVE")) {
                        // Handle a player's move
                        synchronized (TicTacToeServer.this) {
                            if (gameEnded) {
                                // Reject moves if the game has already ended
                                sendMessage("INVALID Game has ended. Please wait for a restart.");
                                continue;
                            }

                            // Determine if it's this player's turn based on their mark
                            String currentTurnMark = getCurrentTurnMark();
                            if (!mark.equals(currentTurnMark)) {
                                // Inform the player that it's not their turn
                                sendMessage("INVALID It's not your turn.");
                                continue;
                            }

                            // Parse the move position from the message
                            int move;
                            try {
                                move = Integer.parseInt(input.substring(5).trim());
                            } catch (NumberFormatException e) {
                                sendMessage("INVALID Invalid move format.");
                                continue;
                            }

                            // Validate the move position
                            if (move < 0 || move > 8) {
                                sendMessage("INVALID Move out of bounds.");
                                continue;
                            }

                            if (!board[move].isEmpty()) {
                                // Reject the move if the cell is already occupied
                                sendMessage("INVALID Cell already occupied.");
                                continue;
                            }

                            // Record the player's move on the board
                            board[move] = mark;

                            // Broadcast the updated board to both players
                            broadcastBoard();

                            // Check if the current move leads to a win
                            if (checkWinner(mark)) {
                                endGame("Congratulations " + playerName + "! You win!", null);
                                continue; // Allow for game restart or termination
                            }

                            // Check if the game is a draw
                            if (isBoardFull()) {
                                endGame("It is a draw!", null);
                                continue; // Allow for game restart or termination
                            }
                        }
                    } else if (input.startsWith("RESTART")) {
                        // Handle a player's request to restart the game
                        synchronized (TicTacToeServer.this) {
                            if (!gameEnded) {
                                // Reject restart requests during an ongoing game
                                sendMessage("INVALID Game is still ongoing.");
                                continue;
                            }

                            // Process the player's decision to restart
                            handleRestartDecision(this);
                            // If both players have agreed to restart, the game state is reset
                        }
                    } else if (input.startsWith("QUIT")) {
                        // Handle a player's request to quit the game
                        synchronized (TicTacToeServer.this) {
                            // Terminate the game session due to the player's quit
                            terminateGame("Player " + playerName + " has quit the game.", this);
                        }
                        break; // Exit the thread as the player has quit
                    }
                }
            } catch (IOException e) {
                // Handle unexpected disconnections
                System.err.println(playerName + " disconnected unexpectedly.");
                synchronized (TicTacToeServer.this) {
                    terminateGame("Player " + playerName + " has disconnected.", this);
                }
            } catch (Exception e) {
                // Handle any other unforeseen errors
                System.err.println("Unexpected error with player " + playerName + ": " + e.getMessage());
                synchronized (TicTacToeServer.this) {
                    terminateGame("Game Ends due to an unexpected error.", this);
                }
            }
        }

        /**
         * Determines the mark of the player whose turn it currently is based on the game board state.
         *
         * <p>
         * The player with mark 'X' always starts first. The turn alternates between players based on the count
         * of marks on the board.
         * </p>
         *
         * @return The mark ("X" or "O") of the player whose turn it is.
         */
        private String getCurrentTurnMark() {
            int xCount = 0;
            int oCount = 0;
            // Count the number of 'X' and 'O' marks on the board
            for (String cell : board) {
                if (cell.equals("X")) xCount++;
                if (cell.equals("O")) oCount++;
            }
            // Determine the current turn based on the counts
            return (xCount == oCount) ? "X" : "O";
        }
    }

    /**
     * The main entry point of the Tic-Tac-Toe server application.
     *
     * @param args Command-line arguments (not utilized).
     */
    public static void main(String[] args) {
        new TicTacToeServer();
    }
}