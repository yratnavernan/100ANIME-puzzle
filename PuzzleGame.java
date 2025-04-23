package puzzlegame;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class PuzzleGame extends JFrame {
    static final int TILE_SIZE = 100;
    static final int GRID_SIZE = 3;
    static final int TOTAL_LEVELS = 100;

    JPanel gamePanel;
    JLabel levelLabel, movesLabel, timerLabel;
    JButton[][] tiles;
    int[][] board;
    int emptyRow, emptyCol;
    int currentLevel = 1;
    int moveCount = 0;
    long startTime;
    Timer gameTimer;
    String assetsFolder = "puzzle_assets";

    public PuzzleGame() {
        setTitle("Java Puzzle Game - 100 Levels");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(GRID_SIZE * TILE_SIZE + 50, GRID_SIZE * TILE_SIZE + 150);
        setResizable(false);
        setLocationRelativeTo(null);

        File assetsDir = new File(assetsFolder);
        if (!assetsDir.exists()) {
            assetsDir.mkdir();
            JOptionPane.showMessageDialog(this,
                    "Assets folder created. Please add puzzle images named 'level1.jpg' to 'level100.jpg'",
                    "Assets Missing", JOptionPane.WARNING_MESSAGE);
        }

        initUI();
        loadLevel(currentLevel);
        startTimer();
    }

    private void initUI() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 3));

        levelLabel = new JLabel("Level: 1/" + TOTAL_LEVELS, SwingConstants.CENTER);
        movesLabel = new JLabel("Moves: 0", SwingConstants.CENTER);
        timerLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);

        controlPanel.add(levelLabel);
        controlPanel.add(movesLabel);
        controlPanel.add(timerLabel);

        gamePanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tiles = new JButton[GRID_SIZE][GRID_SIZE];
        board = new int[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                tiles[row][col] = new JButton();
                tiles[row][col].setFocusPainted(false);
                tiles[row][col].addActionListener(new TileClickListener(this, row, col));
                gamePanel.add(tiles[row][col]);
            }
        }

        JButton resetButton = new JButton("Reset Level");
        resetButton.addActionListener(e -> resetLevel());

        JButton nextButton = new JButton("Next Level");
        nextButton.addActionListener(e -> nextLevel());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(resetButton);
        buttonPanel.add(nextButton);

        add(controlPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    void loadLevel(int level) {
        try {
            BufferedImage levelImage = ImageUtils.loadLevelImage(level, assetsFolder);

            int tileNumber = 1;
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    if (row == GRID_SIZE - 1 && col == GRID_SIZE - 1) {
                        board[row][col] = 0;
                        emptyRow = row;
                        emptyCol = col;
                    } else {
                        board[row][col] = tileNumber++;
                    }

                    if (board[row][col] != 0) {
                        int x = (board[row][col] - 1) % GRID_SIZE;
                        int y = (board[row][col] - 1) / GRID_SIZE;
                        BufferedImage tileImage = levelImage.getSubimage(
                                x * (levelImage.getWidth() / GRID_SIZE),
                                y * (levelImage.getHeight() / GRID_SIZE),
                                levelImage.getWidth() / GRID_SIZE,
                                levelImage.getHeight() / GRID_SIZE);
                        ImageIcon icon = new ImageIcon(tileImage.getScaledInstance(
                                TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH));
                        tiles[row][col].setIcon(icon);
                        tiles[row][col].setText("");
                    } else {
                        tiles[row][col].setIcon(null);
                        tiles[row][col].setText("");
                    }
                }
            }

            shuffleBoard();
            moveCount = 0;
            movesLabel.setText("Moves: 0");
            levelLabel.setText("Level: " + currentLevel + "/" + TOTAL_LEVELS);
            startTimer();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading level " + level + ": " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            createNumberedPuzzle();
        }
    }

    private void createNumberedPuzzle() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (board[row][col] != 0) {
                    tiles[row][col].setIcon(null);
                    tiles[row][col].setText(String.valueOf(board[row][col]));
                } else {
                    tiles[row][col].setIcon(null);
                    tiles[row][col].setText("");
                }
            }
        }
    }

    private void shuffleBoard() {
        Random random = new Random();
        int shuffleMoves = 100 + random.nextInt(100);

        for (int i = 0; i < shuffleMoves; i++) {
            ArrayList<int[]> moves = new ArrayList<>();
            if (emptyRow > 0)
                moves.add(new int[] { emptyRow - 1, emptyCol });
            if (emptyRow < GRID_SIZE - 1)
                moves.add(new int[] { emptyRow + 1, emptyCol });
            if (emptyCol > 0)
                moves.add(new int[] { emptyRow, emptyCol - 1 });
            if (emptyCol < GRID_SIZE - 1)
                moves.add(new int[] { emptyRow, emptyCol + 1 });
            int[] move = moves.get(random.nextInt(moves.size()));
            swapTiles(move[0], move[1], emptyRow, emptyCol);
            emptyRow = move[0];
            emptyCol = move[1];
        }
    }

    private void swapTiles(int r1, int c1, int r2, int c2) {
        int temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;

        Icon icon1 = tiles[r1][c1].getIcon();
        String text1 = tiles[r1][c1].getText();

        tiles[r1][c1].setIcon(tiles[r2][c2].getIcon());
        tiles[r1][c1].setText(tiles[r2][c2].getText());

        tiles[r2][c2].setIcon(icon1);
        tiles[r2][c2].setText(text1);
    }

    void moveTile(int row, int col) {
        if ((Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
                (Math.abs(col - emptyCol) == 1 && row == emptyRow)) {

            swapTiles(row, col, emptyRow, emptyCol);
            emptyRow = row;
            emptyCol = col;
            moveCount++;
            movesLabel.setText("Moves: " + moveCount);

            if (isPuzzleSolved()) {
                gameTimer.stop();
                String time = timerLabel.getText().replace("Time: ", "");
                JOptionPane.showMessageDialog(this,
                        "Level " + currentLevel + " completed!\n" +
                                "Moves: " + moveCount + "\nTime: " + time,
                        "Level Complete", JOptionPane.INFORMATION_MESSAGE);
                if (currentLevel < TOTAL_LEVELS)
                    nextLevel();
                else
                    JOptionPane.showMessageDialog(this,
                            "Congratulations! You've completed all 100 levels!",
                            "Game Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean isPuzzleSolved() {
        int expected = 1;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (row == GRID_SIZE - 1 && col == GRID_SIZE - 1) {
                    if (board[row][col] != 0)
                        return false;
                } else {
                    if (board[row][col] != expected++)
                        return false;
                }
            }
        }
        return true;
    }

    private void resetLevel() {
        loadLevel(currentLevel);
    }

    private void nextLevel() {
        if (currentLevel < TOTAL_LEVELS) {
            currentLevel++;
            loadLevel(currentLevel);
        }
    }

    private void startTimer() {
        if (gameTimer != null)
            gameTimer.stop();
        startTime = System.currentTimeMillis();
        gameTimer = new Timer(1000, e -> updateTimer());
        gameTimer.start();
    }

    private void updateTimer() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PuzzleGame().setVisible(true));
    }
}
