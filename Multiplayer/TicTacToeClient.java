// TicTacToeClient.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * The {@code TicTacToeClient} class implements a two-player Tic Tac Toe game using Java Swing for the graphical user interface.
 * It connects to a Tic-Tac-Toe Server to allow two players to compete against each other over the network.
 * The game includes features such as player name input, real-time clock display, score tracking,
 * and interactive menus for control and help options.
 * </p>
 *
 * <p>
 * Key Features:
 * </p>
 * <ul>
 *     <li>Players must enter their names to start the game.</li>
 *     <li>Displays the current time, updating every second.</li>
 *     <li>Tracks and displays the number of Player 1 wins, Player 2 wins, and draws.</li>
 *     <li>Interactive game board where players use 'X' and 'O'.</li>
 *     <li>Menus for exiting the game and viewing game instructions.</li>
 *     <li>Alerts for game outcomes with options to restart or exit.</li>
 *     <li>Handles disconnections gracefully.</li>
 * </ul>
 * @author
 * @version 1.1
 * @since 2024-04-27
 */
public class TicTacToeClient extends JFrame {
    /**
     * Text field for the player to enter their name.
     */
    private JTextField nameField;

    /**
     * Button to submit the entered player name.
     */
    private JButton submitButton;

    /**
     * Label to display messages and game status to the player.
     */
    private JLabel messageLabel;

    /**
     * Array of buttons representing the 3x3 Tic Tac Toe board.
     */
    private JButton[] boardButtons;

    /**
     * Labels to display the scores for Player 1, Player 2, and Draws.
     */
    private JLabel player1ScoreLabel;
    private JLabel player2ScoreLabel;
    private JLabel drawsLabel;

    /**
     * Label to display the current system time.
     */
    private JLabel clockLabel;

    /**
     * The player's name.
     */
    private String playerName = "";

    /**
     * The player's mark ("X" or "O").
     */
    private String myMark;

    /**
     * The opponent's mark.
     */
    private String opponentMark;

    /**
     * Flag to indicate if it's the player's turn.
     */
    private boolean myTurn = false;

    /**
     * Array to maintain the state of the game board.
     */
    private String[] board = new String[9];

    /**
     * Cumulative Scores
     */
    private int player1Wins = 0;
    private int player2Wins = 0;
    private int draws = 0;

    /**
     * Timer to update the current time label every second.
     */
    private Timer clockTimer;

    /**
     * Socket for communication with the server.
     */
    private Socket socket;

    /**
     * Buffered reader to receive messages from the server.
     */
    private BufferedReader in;

    /**
     * Print writer to send messages to the server.
     */
    private PrintWriter out;

    /**
     * Thread to listen for messages from the server.
     */
    private Thread listenerThread;

    /**
     * Player1 and Player2 Names
     */
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    /**
     * Constructs the TicTacToeClient GUI, connects to the server, initializes the user interface, and starts the clock.
     *
     * @param serverAddress The server's IP address or hostname.
     * @param port          The server's port number.
     */
    public TicTacToeClient(String serverAddress, int port) {
        initializeBoard();
        createUI();
        startClock();

        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start listening to the server
            listenerThread = new Thread(new ServerListener());
            listenerThread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * Initializes the game board by setting all positions to empty strings.
     */
    private void initializeBoard() {
        for (int i = 0; i < 9; i++) {
            board[i] = "";
        }
    }

    /**
     * Creates the graphical user interface components, including menus, panels, labels, and buttons.
     */
    private void createUI() {
        setTitle("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // JMenuBar Setup
        JMenuBar menuBar = new JMenuBar();

        // Control Menu with Exit Item
        JMenu controlMenu = new JMenu("Control");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitGame();
            }
        });
        controlMenu.add(exitItem);
        menuBar.add(controlMenu);

        // Help Menu with Instruction Item
        JMenu helpMenu = new JMenu("Help");
        JMenuItem instructionItem = new JMenuItem("Instruction");
        instructionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showInstructions();
            }
        });
        helpMenu.add(instructionItem);
        menuBar.add(helpMenu);

        // Add JMenuBar to JFrame
        setJMenuBar(menuBar);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                    exitGame();
            }
        });

        // Top Panel for Name Input
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        JLabel nameLabel = new JLabel("Enter your name:");
        topPanel.add(nameLabel);

        nameField = new JTextField(15);
        topPanel.add(nameField);

        submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitName();
            }
        });
        topPanel.add(submitButton);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel for Game Board
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 3, 4, 4)); // Adding gaps between buttons
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        boardButtons = new JButton[9];

        for (int i = 0; i < 9; i++) {
            boardButtons[i] = new JButton("");
            boardButtons[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
            boardButtons[i].setFocusPainted(false);
            final int index = i;
            boardButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sendMove(index);
                }
            });
            boardButtons[i].setEnabled(false); // Disabled until it's the player's turn
            centerPanel.add(boardButtons[i]);
        }

        add(centerPanel, BorderLayout.CENTER);

        // Right Panel for Scores
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 1, 10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Scores"));
        rightPanel.setPreferredSize(new Dimension(200, 0));

        player1ScoreLabel = new JLabel("Player 1 Wins: 0");
        player2ScoreLabel = new JLabel("Player 2 Wins: 0");
        drawsLabel = new JLabel("Draws: 0");

        player1ScoreLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        player2ScoreLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        drawsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        rightPanel.add(player1ScoreLabel);
        rightPanel.add(player2ScoreLabel);
        rightPanel.add(drawsLabel);

        add(rightPanel, BorderLayout.EAST);

        // Bottom Panel for Messages and Clock
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageLabel = new JLabel("Waiting for other player to join...", SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLUE);
        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        bottomPanel.add(messageLabel, BorderLayout.CENTER);

        clockLabel = new JLabel("Current Time: " + getCurrentTime(), SwingConstants.RIGHT);
        clockLabel.setForeground(Color.RED);
        clockLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        bottomPanel.add(clockLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        // Ensure the window is brought to front and gains focus
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                toFront();
                requestFocus();
            }
        });
    }

    /**
     * Handles the submission of the player's name. Validates input, updates the UI, and sends the name to the server.
     */
    private void submitName() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid name.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } else {
            playerName = name;
            // Disable name input
            nameField.setEnabled(false);
            submitButton.setEnabled(false);
            // Send name to server
            out.println("NAME " + playerName);
            setTitle("Tic Tac Toe - " + playerName);
        }
    }

    /**
     * Sends the player's move to the server.
     *
     * @param index The index of the button on the board that was clicked.
     */
    private void sendMove(int index) {
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name before playing.", "Name Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!myTurn || !board[index].isEmpty()) {
            return;
        }

        // Send move to server
        out.println("MOVE " + index);
    }

    /**
     * Starts the clock timer that updates the current time label every second.
     */
    private void startClock() {
        clockTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clockLabel.setText("Current Time: " + getCurrentTime());
            }
        });
        clockTimer.start();
    }

    /**
     * Retrieves the current system time in "HH:mm:ss" format.
     *
     * @return A string representing the current time.
     */
    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }

    /**
     * Updates the board UI based on the current game state.
     */
    private void updateBoardUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (int i = 0; i < 9; i++) {
                    boardButtons[i].setText(board[i]);
                    boardButtons[i].setEnabled(false);
                    if (board[i].isEmpty()) {
                        boardButtons[i].setEnabled(myTurn);
                    }
                }
            }
        });
    }

    /**
     * Updates the score labels to reflect the current number of Player1 wins, Player2 wins, and draws.
     */
    private void updateScores() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                player1ScoreLabel.setText("Player 1 Wins: " + player1Wins);
                player2ScoreLabel.setText("Player 2 Wins: " + player2Wins);
                drawsLabel.setText("Draws: " + draws);
            }
        });
    }

    /**
     * Displays a dialog indicating the game's outcome and prompts the player to play again or exit.
     * Disables the game board buttons to prevent further moves until a decision is made.
     *
     * @param message The message to display in the dialog, indicating the game's outcome.
     */
    private void showEndGameDialog(String message) {
        // Disable all buttons to prevent further moves
        for (JButton btn : boardButtons) {
            btn.setEnabled(false);
        }

        int response = JOptionPane.showConfirmDialog(this, message + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.NO_OPTION) {
        	exitGame();
        } else {
            resetBoard();
            out.println("RESTART");

        }
    }

    /**
     * Resets the game board for a new game, clearing all marks and re-enabling the board buttons.
     * Maintains the player's name and current scores.
     */
    private void resetBoard() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (int i = 0; i < 9; i++) {
                    board[i] = "";
                    boardButtons[i].setText("");
                    boardButtons[i].setEnabled(false);
                }
                myTurn = (myMark.equals("X")); // Player with "X" starts first
                messageLabel.setText(myTurn ? "Your turn." : "Valid Move, Waiting for opponent's move.");
            }
        });
    }

    /**
     * Handles the action of exiting the game. Prompts the user for confirmation before terminating the application.
     */
    private void exitGame() {
        try {
            out.println("QUIT");
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
        System.exit(0);
    }

    /**
     * Displays a dialog containing game instructions to guide the player.
     */
    private void showInstructions() {
        String instructions = "<html><h3>Some Information about the game</h3>"
                + "<p>Criteria for a valid move:</p>"
                + "<p>- The move is not occupied by any mark.</p>"
                + "<p>- The move is made in the player’s turn.</p>"
                + "<p>- The move is made within the 3 x 3 board.</p>"
                + "<p>The game would continue and switch among the opposite player until it reaches either\n"
                + "one of the following conditions:</p>"
                + "<p>- Player 1 wins.</p>"
                + "<p>- Player 2 wins.</p>"
                + "<p>- Draw</p>"
                + "</html>";
        JOptionPane.showMessageDialog(this, instructions, "Game Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * The main method to launch the Tic-Tac-Toe client application.
     *
     * @param args Command-line arguments: none is required
     */
    public static void main(String[] args) {
        // Temporary variables to hold server address and port
        String ServerAddress = "127.0.0.1";
        int Port = 7000;


        // Ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TicTacToeClient(ServerAddress, Port);
            }
        });
    }
    
    /**
     * Handles incoming messages from the server.
     */
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Received from server: " + response); // Debugging Line
                    if (response.startsWith("WELCOME")) {
                        // Welcome message
                        messageLabel.setText(response.substring(8));
                    } else if (response.startsWith("TITLE")) {
                        // Set frame title
                        String title = response.substring(6);
                        setTitle(title);
                    } else if (response.startsWith("MESSAGE")) {
                        // Game messages
                        String msg = response.substring(8);
                        messageLabel.setText(msg);
                    } else if (response.startsWith("START")) {
                        // Start the game and assign marks
                        myMark = response.substring(6).trim();
                        opponentMark = myMark.equals("X") ? "O" : "X";
                        myTurn = myMark.equals("X"); // Player with "X" starts first
                        messageLabel.setText(myTurn ? "Your turn." : "Waiting for opponent's move.");
                        enableBoardButtons();
                    } else if (response.startsWith("PLAYER1")) {
                        // Set Player1's name
                        player1Name = response.substring(8).trim();
                        updateScores();
                    } else if (response.startsWith("PLAYER2")) {
                        // Set Player2's name
                        player2Name = response.substring(8).trim();
                        updateScores();
                    } else if (response.startsWith("BOARD")) {
                        // Update board state
                        String boardState = response.substring(6);
                        String[] cells = boardState.split(",", -1); // Preserve trailing empty strings
                        if (cells.length != 9) {
                            System.err.println("Invalid board state received: " + boardState);
                            continue; // Skip this iteration to prevent errors
                        }
                        for (int i = 0; i < 9; i++) {
                            board[i] = cells[i].equals("-") ? "" : cells[i];
                        }
                        updateBoardUI();

                        // Recalculate 'myTurn' based on the current board state
                        int xCount = 0;
                        int oCount = 0;
                        for (String cell : board) {
                            if (cell.equals("X")) xCount++;
                            if (cell.equals("O")) oCount++;
                        }
                        if (myMark.equals("X")) {
                            myTurn = (xCount == oCount);
                        } else {
                            myTurn = (xCount > oCount);
                        }
                        messageLabel.setText(myTurn ? "Your turn." : "Waiting for opponent's move.");
                        enableBoardButtons();
                    } else if (response.startsWith("SCORE")) {
                        // Update cumulative scores
                        String[] tokens = response.split(" ");
                        if (tokens.length >= 4) {
                            try {
                                player1Wins = Integer.parseInt(tokens[1]);
                                player2Wins = Integer.parseInt(tokens[2]);
                                draws = Integer.parseInt(tokens[3]);
                                updateScores();
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid score format received.");
                            }
                        }
                    } else if (response.startsWith("INVALID")) {
                        // Invalid move attempt
                        String msg = response.substring(8).trim();
                        JOptionPane.showMessageDialog(TicTacToeClient.this, msg, "Invalid Move", JOptionPane.WARNING_MESSAGE);
                    } else if (response.startsWith("END")) {
                        // Game end message
                        String msg = response.substring(4).trim();
                        showEndGameDialog(msg);
                        // Continue listening for potential restarts
                    } else if (response.startsWith("Quit")) {
                    	JOptionPane.showMessageDialog(TicTacToeClient.this, "Game Ends. One of the players left.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    	System.exit(0);
                    } else {
                        // Handle unexpected messages
                        System.out.println("Unknown message type received: " + response);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(TicTacToeClient.this, "Connection to server lost.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        /**
         * Enables or disables the board buttons based on the player's turn.
         */
        private void enableBoardButtons() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (int i = 0; i < 9; i++) {
                        if (board[i].isEmpty()) {
                            boardButtons[i].setEnabled(myTurn);
                        } else {
                            boardButtons[i].setEnabled(false);
                        }
                    }
                }
            });
        }
    }
}