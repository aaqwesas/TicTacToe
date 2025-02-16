import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * The {@code TicTacToeGame} class implements a classic Tic Tac Toe game using Java Swing for the graphical user interface.
 * It allows a single player to compete against the computer. The game includes features such as player name input,
 * real-time clock display, score tracking, and interactive menus for control and help options.
 * </p>
 *
 * <p>
 * Key Features:
 * </p>
 * <ul>
 *     <li>Player must enter a name to start the game.</li>
 *     <li>Displays the current time, updating every second.</li>
 *     <li>Tracks and displays the number of wins, losses, and draws.</li>
 *     <li>Interactive game board where the player uses 'X' and the computer uses 'O'.</li>
 *     <li>Menus for exiting the game and viewing game instructions.</li>
 *     <li>Alerts for game outcomes with options to restart or exit.</li>
 * </ul>
 *
 * @author
 * @version 1.0
 * @since 2024-11-14
 */
public class TicTacToeGame extends JFrame {
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
     * Label to display the number of player wins.
     */
    private JLabel winsLabel;

    /**
     * Label to display the number of computer wins.
     */
    private JLabel lossesLabel;

    /**
     * Label to display the number of draw games.
     */
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
     * Flag to indicate if it's the player's turn.
     */
    private boolean playerTurn = true; // Player starts first

    /**
     * Array to maintain the state of the game board.
     */
    private String[] board = new String[9];

    /**
     * Counter for the number of player wins.
     */
    private int wins = 0;

    /**
     * Counter for the number of computer wins.
     */
    private int losses = 0;

    /**
     * Counter for the number of draws.
     */
    private int draws = 0;

    /**
     * Timer to update the current time label every second.
     */
    private Timer clockTimer;

    /**
     * Timer to delay the computer's move by 2 seconds.
     */
    private Timer computerMoveTimer;

    /**
     * Random number generator for the computer's move selection.
     */
    private Random random = new Random();

    /**
     * Constructs the TicTacToeGame GUI, initializes the game board, user interface, and starts the clock.
     */
    public TicTacToeGame() {
        initializeBoard();
        createUI();
        startClock();
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                    playerMove(index);
                }
            });
            centerPanel.add(boardButtons[i]);
        }

        add(centerPanel, BorderLayout.CENTER);

        // Right Panel for Scores
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 1, 10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        winsLabel = new JLabel("Player Wins: 0");
        lossesLabel = new JLabel("Computer Wins: 0");
        drawsLabel = new JLabel("Draws: 0");

        winsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        lossesLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        drawsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));

        rightPanel.add(winsLabel);
        rightPanel.add(lossesLabel);
        rightPanel.add(drawsLabel);

        add(rightPanel, BorderLayout.EAST);

        // Bottom Panel for Messages and Clock
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageLabel = new JLabel("Enter your player name...", SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLUE);
        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        bottomPanel.add(messageLabel, BorderLayout.CENTER);

        clockLabel = new JLabel("Current Time: " + getCurrentTime(), SwingConstants.RIGHT);
        clockLabel.setForeground(Color.RED);
        clockLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        bottomPanel.add(clockLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Make the window visible before invoking toFront
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
     * Handles the submission of the player's name. Validates input, updates the UI, and disables further name entry.
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
            // Update title and message
            setTitle("Tic Tac Toe - Player: " + playerName);
            messageLabel.setText("WELCOME " + playerName);
        }
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
     * Processes the player's move when a board button is clicked. Validates the move, updates the board,
     * checks for game outcomes, and initiates the computer's move if the game continues.
     *
     * @param index The index of the button on the board that was clicked.
     */
    private void playerMove(int index) {
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name before playing.", "Name Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!playerTurn || !board[index].isEmpty()) {
            return;
        }

        // Player makes a move
        board[index] = "X"; // Use "X" for player
        updateButton(index, "X"); // Update button with mark
        messageLabel.setText("Valid move, waiting for your opponent.");

        if (checkWinner("X")) {
            wins++;
            showEndGameDialog("Congratulations " + playerName + ", you win!");
            updateScores();
            return;
        } else if (isBoardFull()) {
            draws++;
            showEndGameDialog("It's a draw!");
            updateScores();
            return;
        }
        

        playerTurn = false;

        // Schedule computer's move after 2 seconds
        computerMoveTimer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                computerMove();
                computerMoveTimer.stop();
            }
        });
        computerMoveTimer.setRepeats(false);
        computerMoveTimer.start();

        messageLabel.setText("Valid move, waiting for your opponent.");
    }

    /**
     * Updates the specified board button with the given mark ("X" or "O") and disables the button to prevent re-selection.
     *
     * @param index The index of the button on the board to update.
     * @param mark  The mark to place on the button ("X" or "O").
     */
    private void updateButton(int index, String mark) {
        boardButtons[index].setText(mark);
        boardButtons[index].setEnabled(false);
    }

    /**
     * Executes the computer's move by selecting a random available spot on the board. Updates the board,
     * checks for game outcomes, and updates the game state accordingly.
     */
    private void computerMove() {
        // Find available moves
        java.util.List<Integer> availableMoves = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (board[i].isEmpty()) {
                availableMoves.add(i);
            }
        }

        if (availableMoves.isEmpty()) {
            return;
        }

        // Choose a random move
        int move = availableMoves.get(random.nextInt(availableMoves.size()));
        board[move] = "O"; // Use "O" for computer
        updateButton(move, "O"); // Update button with mark

        if (checkWinner("O")) {
            losses++;
            updateScores();
            showEndGameDialog("Computer wins! Better luck next time.");
            return;
        } else if (isBoardFull()) {
            draws++;
            updateScores();
            showEndGameDialog("It's a draw!");
            return;
        }

        playerTurn = true;
        messageLabel.setText("Your opponent has moved, now is your turn.");
    }

    /**
     * Checks if the specified player has achieved a winning condition on the board.
     *
     * @param player The player to check for a win ("X" or "O").
     * @return {@code true} if the player has won; {@code false} otherwise.
     */
    private boolean checkWinner(String player) {
        int[][] winConditions = {
            {0, 1, 2}, // Rows
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6}, // Columns
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8}, // Diagonals
            {2, 4, 6}
        };

        for (int[] condition : winConditions) {
            if (board[condition[0]].equals(player) &&
                board[condition[1]].equals(player) &&
                board[condition[2]].equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the game board is completely filled with marks, resulting in a draw.
     *
     * @return {@code true} if the board is full; {@code false} otherwise.
     */
    private boolean isBoardFull() {
        for (String cell : board) {
            if (cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the score labels to reflect the current number of wins, losses, and draws.
     */
    private void updateScores() {
        winsLabel.setText("Player Wins: " + wins);
        lossesLabel.setText("Computer Wins: " + losses);
        drawsLabel.setText("Draws: " + draws);
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
            // Exit the application
            System.exit(0);

        } else {
            resetBoard();
        }
    }

    /**
     * Resets the game board for a new game, clearing all marks and re-enabling the board buttons.
     * Maintains the player's name and current scores.
     */
    private void resetBoard() {
        for (int i = 0; i < 9; i++) {
            board[i] = "";
            boardButtons[i].setText("");
            boardButtons[i].setEnabled(true);
        }
        playerTurn = true;
        messageLabel.setText("WELCOME " + playerName);
    }

    /**
     * Handles the action of exiting the game. Prompts the user for confirmation before terminating the application.
     */
    private void exitGame() {
        System.exit(0);
    }

    /**
     * Displays a dialog containing game instructions to guide the player.
     */
    private void showInstructions() {
        String instructions = "<html><h3>Some Infotmation about the game:</h3>"
                + "<p>- The move is not occupied by any mark.</p>"
                + "<p>- The move is make in the player's turn.</p>"
                + "<p>- The move is made within the 3 x 3 board.</p>"
                + "<h3>The game would continue and switch among the player until it reaches either one of the following conditions:</h3>"
                + "<p>- Player wins. </p>"
                + "<p>- Computer wins.</p>"
                + "<p>- Draw.</p>"
                + "</html>";
        JOptionPane.showMessageDialog(this, instructions, "Game Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * The main method to launch the Tic Tac Toe game application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TicTacToeGame();
            }
        });
    }
}