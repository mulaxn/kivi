import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class KiviGameplay extends JFrame {
    // Game board components
    private JPanel gameBoard;
    private JButton[][] boardSquares;
    private JPanel dicePanel;
    private JButton[] dice;
    private JButton rollDiceButton, endTurnButton;
    private JButton pauseButton, resumeButton;
    private JPanel playerInfoPanel;
    private JLabel currentPlayerLabel, timerLabel;
    
    // Game state variables
    private int currentPlayer = 0;
    private int playerCount;
    private String[] playerNames;
    private Color[] playerColors;
    private boolean[] isHuman;
    private int[] stonesLeft;
    private int turnTimeTotal; // total time for a turn in seconds
    private int[] playerScores;
    
    // Timer variables
    private Timer gameTimer;
    private long turnStartTime;  // records the system time (in ms) when a turn starts
    
    // Dice and roll info
    private boolean[] diceSelected;
    private int[] diceValues;
    private int rollCount;  // 0 = no roll, then 1st roll, etc.
    
    // Board constants
    private final int BOARD_SIZE = 7;
    private final String[] SQUARE_TYPES = {
        "AA/BB", "ABCDE", "≤12", "AAA", "=1,3,5", "=2,4,6", "AAA",
        "=2,4,6", "AAAA/BB", "AAA", "AA/BB/CC", "ABCD", "AAA/BBB", "≥30",
        "ABCD", "AAAA", "≥30", "ABCDE", "AAAA/BB", "=1,3,5", "AAA/BB",
        "≤12", "AAA/BB", "=2,4,6", "AAA/BBB", "≤12", "AA/BB", "ABCDE",
        "AAA", "ABCDE", "AA/BB/CC", "=1,3,5", "AAAA", "≥30", "AA/BB",
        "=1,3,5", "AAA/BBB", "ABCD", "AAAA/BB", "AAA/BB", "AA/BB/CC", "≤12",
        "ABCD", "≥30", "AAAA", "AA/BB", "=1,3,5", "AAAA", "AAA/BB"
    };
    
    // To store each square's base color so we can reset after highlighting
    private Color[] originalColors;
    
    // Colors for squares based on points:
    private final Color WHITE_SQUARE = Color.WHITE;
    private final Color PINK_SQUARE = new Color(255, 182, 193);
    private final Color HOT_PINK_SQUARE = new Color(255, 105, 180);
    
    // Variables to hold the currently placed (tentative) stone
    private int currentStoneRow = -1;
    private int currentStoneCol = -1;
    private StonePanel currentStone = null;
    
    // Pause flag
    private boolean isPaused = false;
    
    public KiviGameplay(int playerCount, String[] playerNames, Color[] playerColors, boolean[] isHuman, int turnTime) {
        super("KIVI - Game");
        this.playerCount = playerCount;
        this.playerNames = playerNames;
        this.playerColors = playerColors;
        this.isHuman = isHuman;
        this.turnTimeTotal = turnTime;
        
        stonesLeft = new int[playerCount];
        playerScores = new int[playerCount];
        for (int i = 0; i < playerCount; i++) {
            stonesLeft[i] = 10;
            playerScores[i] = 0;
        }
        
        diceValues = new int[6];
        diceSelected = new boolean[6];
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout(10, 10));
        
        createGameBoard();
        createDicePanel();
        createPlayerInfoPanel();
        
        startGame();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Returns points based on the square type.
    private int getPointsForSquareType(String squareType) {
        switch (squareType) {
            case "AA/BB":
            case "AAA":
            case "ABCD":
            case "AAA/BB":
                return 1;
            case "AAAA":
            case "ABCDE":
            case "≤12":
            case "≥30":
                return 2;
            case "AA/BB/CC":
            case "AAA/BBB":
            case "AAAA/BB":
                return 3;
            default:
                return 0;
        }
    }
    
    private void createGameBoard() {
        gameBoard = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 2, 2));
        gameBoard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        boardSquares = new JButton[BOARD_SIZE][BOARD_SIZE];
        originalColors = new Color[BOARD_SIZE * BOARD_SIZE];
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int index = i * BOARD_SIZE + j;
                String squareType = SQUARE_TYPES[index];
                int points = getPointsForSquareType(squareType);
                Color squareColor;
                if (points == 1 || points == 0) {
                    squareColor = WHITE_SQUARE;
                } else if (points == 2) {
                    squareColor = PINK_SQUARE;
                } else {
                    squareColor = HOT_PINK_SQUARE;
                }
                originalColors[index] = squareColor;
                
                JButton square = new JButton();
                square.setPreferredSize(new Dimension(80, 80));
                square.setBackground(DisplaySettings.ColorBlindnessFilter.transformColor(squareColor));
                square.setOpaque(true);
                square.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                square.setFocusPainted(false);
                
                square.setLayout(new BorderLayout());
                JLabel typeLabel = new JLabel(squareType, JLabel.CENTER);
                typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
                square.add(typeLabel, BorderLayout.CENTER);
                
                final int row = i;
                final int col = j;
                square.addActionListener(e -> attemptPlacePiece(row, col));
                
                boardSquares[i][j] = square;
                gameBoard.add(square);
            }
        }
        
        add(gameBoard, BorderLayout.CENTER);
    }
    
    private void createDicePanel() {
        dicePanel = new JPanel(new FlowLayout());
        
        dice = new JButton[6];
        for (int i = 0; i < 6; i++) {
            dice[i] = new JButton("?");
            dice[i].setPreferredSize(new Dimension(60, 60));
            dice[i].setFont(new Font("Arial", Font.BOLD, 20));
            final int dieIndex = i;
            dice[i].addActionListener(e -> toggleDieSelection(dieIndex));
            dicePanel.add(dice[i]);
        }
        
        rollDiceButton = new JButton("Roll Dice");
        rollDiceButton.addActionListener(e -> rollDice());
        dicePanel.add(rollDiceButton);
        
        endTurnButton = new JButton("End Turn");
        endTurnButton.setEnabled(false);
        endTurnButton.addActionListener(e -> endTurn());
        dicePanel.add(endTurnButton);
        
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pauseGame());
        dicePanel.add(pauseButton);
        
        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> resumeGame());
        resumeButton.setEnabled(false);
        dicePanel.add(resumeButton);
        
        add(dicePanel, BorderLayout.SOUTH);
    }
    
    private void createPlayerInfoPanel() {
        playerInfoPanel = new JPanel();
        playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.Y_AXIS));
        playerInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        currentPlayerLabel = new JLabel("Current Player: " + playerNames[currentPlayer]);
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerInfoPanel.add(currentPlayerLabel);
        
        playerInfoPanel.add(Box.createVerticalStrut(10));
        
        // Initially show the total time for the turn.
        timerLabel = new JLabel("Time left: " + turnTimeTotal + "s");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        playerInfoPanel.add(timerLabel);
        
        playerInfoPanel.add(Box.createVerticalStrut(20));
        
        updatePlayerStats();
        
        add(playerInfoPanel, BorderLayout.EAST);
    }
    
    // Update player statistics without recreating the time label
    private void updatePlayerStats() {
        // Remove only player stats components (keep currentPlayerLabel and timerLabel)
        Component[] components = playerInfoPanel.getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];
            if (!(comp == currentPlayerLabel || comp == timerLabel || comp instanceof Box.Filler)) {
                playerInfoPanel.remove(comp);
            }
        }
        
        // Add updated player stats
        for (int i = 0; i < playerCount; i++) {
            JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel nameLabel = new JLabel(playerNames[i] + " - Stones: " + stonesLeft[i] + " | Score: " + playerScores[i]);
            nameLabel.setForeground(playerColors[i]);
            playerPanel.add(nameLabel);
            playerInfoPanel.add(playerPanel);
        }
        
        playerInfoPanel.revalidate();
        playerInfoPanel.repaint();
    }
    
    // Starts a turn by recording the start time and starting a Timer with 100ms delay.
    private void startTurn() {
        // Reset turn time by recording current time
        turnStartTime = System.currentTimeMillis();
        rollCount = 0;
        for (int i = 0; i < 6; i++) {
            diceSelected[i] = false;
            dice[i].setText("?");
            dice[i].setBackground(null);
            dice[i].setEnabled(true);
        }
        if (currentStone != null && currentStoneRow >= 0 && currentStoneCol >= 0) {
            boardSquares[currentStoneRow][currentStoneCol].remove(currentStone);
            boardSquares[currentStoneRow][currentStoneCol].revalidate();
            boardSquares[currentStoneRow][currentStoneCol].repaint();
            currentStone = null;
            currentStoneRow = -1;
            currentStoneCol = -1;
        }
        currentPlayerLabel.setText("Current Player: " + playerNames[currentPlayer]);
        rollDiceButton.setEnabled(true);
        endTurnButton.setEnabled(false);
        // Set timer label to show full turn time initially
        timerLabel.setText("Time left: " + turnTimeTotal + ".0s");
        
        // Create and start a timer that ticks every 100ms for smooth updates.
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        gameTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - turnStartTime;
                int remainingSec = turnTimeTotal - (int)(elapsed / 1000);
                int dec = (int)((elapsed % 1000) / 100);
                if (remainingSec < 0) {
                    remainingSec = 0;
                }
                timerLabel.setText("Time left: " + remainingSec + "." + dec + "s");
                if (elapsed >= turnTimeTotal * 1000) {
                    gameTimer.stop();
                    endTurn();
                }
            }
        });
        gameTimer.start();
        
        // Run CPU turn on a separate thread so its dice roll is visible.
        if (!isHuman[currentPlayer]) {
            new Thread(() -> handleCpuTurn()).start();
        }
    }
    
    private void startGame() {
        currentPlayer = 0;
        startTurn();
    }
    
    private void rollDice() {
        if (isPaused) return;
        if (currentStone != null && currentStoneRow >= 0 && currentStoneCol >= 0) {
            boardSquares[currentStoneRow][currentStoneCol].remove(currentStone);
            boardSquares[currentStoneRow][currentStoneCol].revalidate();
            boardSquares[currentStoneRow][currentStoneCol].repaint();
            currentStone = null;
            currentStoneRow = -1;
            currentStoneCol = -1;
        }
        Random random = new Random();
        if (rollCount == 0) {
            for (int i = 0; i < 6; i++) {
                diceValues[i] = random.nextInt(6) + 1;
                dice[i].setText(String.valueOf(diceValues[i]));
                diceSelected[i] = false;
                dice[i].setBackground(null);
            }
        } else if (rollCount < 3) {
            boolean anySelected = false;
            for (int i = 0; i < 6; i++) {
                if (diceSelected[i]) {
                    anySelected = true;
                    break;
                }
            }
            if (!anySelected) {
                for (int i = 0; i < 6; i++) {
                    diceValues[i] = random.nextInt(6) + 1;
                    dice[i].setText(String.valueOf(diceValues[i]));
                    diceSelected[i] = false;
                    dice[i].setBackground(null);
                }
            } else {
                for (int i = 0; i < 6; i++) {
                    if (!diceSelected[i]) {
                        diceValues[i] = random.nextInt(6) + 1;
                        dice[i].setText(String.valueOf(diceValues[i]));
                    }
                }
            }
        }
        rollCount++;
        if (rollCount >= 3) {
            rollDiceButton.setEnabled(false);
        }
        highlightValidMoves();
    }
    
    private void toggleDieSelection(int dieIndex) {
        if (isPaused) return;
        if (rollCount == 0) return;
        diceSelected[dieIndex] = !diceSelected[dieIndex];
        if (diceSelected[dieIndex]) {
            dice[dieIndex].setBackground(playerColors[currentPlayer]);
        } else {
            dice[dieIndex].setBackground(null);
        }
        highlightValidMoves();
    }
    
    private void highlightValidMoves() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int index = i * BOARD_SIZE + j;
                boardSquares[i][j].setBackground(DisplaySettings.ColorBlindnessFilter.transformColor(originalColors[index]));
            }
        }
        if (rollCount == 0) return;
        ArrayList<Integer> selectedValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (diceSelected[i]) {
                selectedValues.add(diceValues[i]);
            }
        }
        if (selectedValues.isEmpty()) return;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (boardSquares[i][j].getComponentCount() > 1 && !(i == currentStoneRow && j == currentStoneCol))
                    continue;
                int index = i * BOARD_SIZE + j;
                String squareType = SQUARE_TYPES[index];
                if (isValidPlacement(squareType, selectedValues)) {
                    boardSquares[i][j].setBackground(DisplaySettings.ColorBlindnessFilter.transformColor(Color.GREEN.brighter()));
                }
            }
        }
    }
    
    private boolean isValidPlacement(String squareType, ArrayList<Integer> selectedValues) {
        if (selectedValues.isEmpty()) return false;
        selectedValues.sort(null);
        switch (squareType) {
            case "≤12": {
                int sum = 0;
                for (int value : selectedValues) {
                    sum += value;
                }
                return sum <= 12;
            }
            case "≥30": {
                int sum = 0;
                for (int value : selectedValues) {
                    sum += value;
                }
                return sum >= 30;
            }
            case "=1,3,5":
                return (selectedValues.contains(1) && selectedValues.contains(3) && selectedValues.contains(5));
            case "=2,4,6":
                return (selectedValues.contains(2) && selectedValues.contains(4) && selectedValues.contains(6));
            case "ABCDE":
                return (selectedValues.contains(1) && selectedValues.contains(2) &&
                        selectedValues.contains(3) && selectedValues.contains(4) &&
                        selectedValues.contains(5));
            case "ABCD":
                return (selectedValues.contains(1) && selectedValues.contains(2) &&
                        selectedValues.contains(3) && selectedValues.contains(4));
            case "AAA":
                return hasAtLeastCount(selectedValues, 3);
            case "AAAA":
                return hasAtLeastCount(selectedValues, 4);
            case "AA/BB":
                return hasPairs(selectedValues, 2);
            case "AA/BB/CC":
                return hasPairs(selectedValues, 3);
            case "AAA/BB":
                return hasFullHouse(selectedValues);
            case "AAAA/BB":
                return hasFourAndPair(selectedValues);
            case "AAA/BBB":
                return hasTwoTriplets(selectedValues);
            default:
                return false;
        }
    }
    
    private boolean hasAtLeastCount(ArrayList<Integer> values, int needed) {
        int[] counts = new int[7];
        for (int v : values) counts[v]++;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= needed) return true;
        }
        return false;
    }
    
    private boolean hasPairs(ArrayList<Integer> values, int pairsNeeded) {
        int[] counts = new int[7];
        for (int v : values) counts[v]++;
        int pairCount = 0;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 2) pairCount++;
        }
        return pairCount >= pairsNeeded;
    }
    
    private boolean hasFullHouse(ArrayList<Integer> values) {
        int[] counts = new int[7];
        for (int v : values) counts[v]++;
        boolean hasThree = false;
        boolean hasPair = false;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 3) hasThree = true;
            else if (counts[i] >= 2) hasPair = true;
        }
        return hasThree && hasPair;
    }
    
    private boolean hasFourAndPair(ArrayList<Integer> values) {
        int[] counts = new int[7];
        for (int v : values) counts[v]++;
        boolean hasFour = false;
        boolean hasPair = false;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 4) hasFour = true;
            else if (counts[i] >= 2) hasPair = true;
        }
        return hasFour && hasPair;
    }
    
    private boolean hasTwoTriplets(ArrayList<Integer> values) {
        int[] counts = new int[7];
        for (int v : values) counts[v]++;
        int tripletCount = 0;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 3) tripletCount++;
        }
        return tripletCount >= 2;
    }
    
    private void attemptPlacePiece(int row, int col) {
        if (!isHuman[currentPlayer]) return;
        if (rollCount == 0) return;
        if (boardSquares[row][col].getComponentCount() > 1 && !(row == currentStoneRow && col == currentStoneCol)) {
            JOptionPane.showMessageDialog(this, "This square is already occupied!");
            return;
        }
        ArrayList<Integer> selectedValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (diceSelected[i]) {
                selectedValues.add(diceValues[i]);
            }
        }
        if (selectedValues.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select dice first!");
            return;
        }
        int index = row * BOARD_SIZE + col;
        String squareType = SQUARE_TYPES[index];
        if (!isValidPlacement(squareType, selectedValues)) {
            JOptionPane.showMessageDialog(this, "Invalid placement! This combination doesn't match the square requirements.");
            return;
        }
        placePiece(row, col);
    }
    
    private void placePiece(int row, int col) {
        if (currentStone != null && currentStoneRow >= 0 && currentStoneCol >= 0) {
            boardSquares[currentStoneRow][currentStoneCol].remove(currentStone);
            boardSquares[currentStoneRow][currentStoneCol].revalidate();
            boardSquares[currentStoneRow][currentStoneCol].repaint();
        }
        currentStone = new StonePanel(playerColors[currentPlayer]);
        currentStone.setPreferredSize(new Dimension(40, 40));
        boardSquares[row][col].add(currentStone, BorderLayout.CENTER);
        boardSquares[row][col].revalidate();
        boardSquares[row][col].repaint();
        currentStoneRow = row;
        currentStoneCol = col;
        endTurnButton.setEnabled(true);
    }
    
    private void endTurn() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        if (currentStone != null && currentStoneRow >= 0 && currentStoneCol >= 0) {
            int index = currentStoneRow * BOARD_SIZE + currentStoneCol;
            String squareType = SQUARE_TYPES[index];
            ArrayList<Integer> selectedValues = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                if (diceSelected[i]) {
                    selectedValues.add(diceValues[i]);
                }
            }
            int points = calculatePoints(squareType, selectedValues);
            playerScores[currentPlayer] += points;
            stonesLeft[currentPlayer]--;
            currentStone = null;
            currentStoneRow = -1;
            currentStoneCol = -1;
        }
        for (int i = 0; i < 6; i++) {
            dice[i].setEnabled(false);
        }
        rollDiceButton.setEnabled(false);
        
        updatePlayerStats();
        currentPlayer = (currentPlayer + 1) % playerCount;
        if (isGameOver()) {
            endGame();
            return;
        }
        startTurn();
    }
    
    private int calculatePoints(String squareType, ArrayList<Integer> diceUsed) {
        switch (squareType) {
            case "AA/BB":
            case "AAA":
            case "ABCD":
            case "AAA/BB":
                return 1;
            case "AAAA":
            case "ABCDE":
            case "≤12":
            case "≥30":
                return 2;
            case "AA/BB/CC":
            case "AAA/BBB":
            case "AAAA/BB":
                return 3;
            default:
                return 0;
        }
    }
    
    private boolean isGameOver() {
        for (int i = 0; i < playerCount; i++) {
            if (stonesLeft[i] <= 0) {
                return true;
            }
        }
        boolean boardFull = true;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (boardSquares[i][j].getComponentCount() <= 1) {
                    boardFull = false;
                    break;
                }
            }
            if (!boardFull) break;
        }
        return boardFull;
    }
    
    private void endGame() {
        int[] scores = calculateScores();
        int maxScore = -1;
        int winner = -1;
        for (int i = 0; i < playerCount; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                winner = i;
            }
        }
        StringBuilder message = new StringBuilder();
        message.append("Game Over!\n\n");
        message.append("Final Scores:\n");
        for (int i = 0; i < playerCount; i++) {
            message.append(playerNames[i]).append(": ").append(scores[i]).append(" points\n");
        }
        message.append("\nWinner: ").append(playerNames[winner]).append("!");
        JOptionPane.showMessageDialog(this, message.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
    
    private int[] calculateScores() {
        return playerScores;
    }
    
    private void handleCpuTurn() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rollDice();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean foundMove = false;
        for (int numDice = 2; numDice <= 6 && !foundMove; numDice++) {
            boolean[] selected = new boolean[6];
            foundMove = tryDiceCombination(selected, 0, 0, numDice);
            if (foundMove) {
                for (int i = 0; i < 6; i++) {
                    diceSelected[i] = selected[i];
                    dice[i].setBackground(selected[i] ? playerColors[currentPlayer] : null);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < BOARD_SIZE && currentStone == null; i++) {
                    for (int j = 0; j < BOARD_SIZE && currentStone == null; j++) {
                        if (boardSquares[i][j].getComponentCount() > 1) continue;
                        String squareType = SQUARE_TYPES[i * BOARD_SIZE + j];
                        ArrayList<Integer> selValues = new ArrayList<>();
                        for (int k = 0; k < 6; k++) {
                            if (diceSelected[k]) {
                                selValues.add(diceValues[k]);
                            }
                        }
                        if (isValidPlacement(squareType, selValues)) {
                            placePiece(i, j);
                            break;
                        }
                    }
                }
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTurn();
    }
    
    private boolean tryDiceCombination(boolean[] selected, int index, int count, int target) {
        if (count == target) {
            ArrayList<Integer> selValues = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                if (selected[i]) {
                    selValues.add(diceValues[i]);
                }
            }
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (boardSquares[i][j].getComponentCount() > 1) continue;
                    String squareType = SQUARE_TYPES[i * BOARD_SIZE + j];
                    if (isValidPlacement(squareType, selValues)) {
                        return true;
                    }
                }
            }
            return false;
        }
        if (index >= 6) return false;
        selected[index] = true;
        if (tryDiceCombination(selected, index + 1, count + 1, target)) return true;
        selected[index] = false;
        return tryDiceCombination(selected, index + 1, count, target);
    }
    
    private void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            if (gameTimer != null) {
                gameTimer.stop();
            }
            setGameControlButtonsEnabled(false);
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
            System.out.println("Game paused.");
        }
    }
    
    private void resumeGame() {
        if (isPaused) {
            isPaused = false;
            if (gameTimer != null) {
                gameTimer.start();
            }
            setGameControlButtonsEnabled(true);
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
            System.out.println("Game resumed.");
        }
    }
    
    private void setGameControlButtonsEnabled(boolean enabled) {
        for (JButton d : dice) {
            d.setEnabled(enabled);
        }
        rollDiceButton.setEnabled(enabled && rollCount < 3);
        endTurnButton.setEnabled(enabled && currentStone != null);
    }
    
    @Override
    public void dispose() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        super.dispose();
    }
    
    // Inner class for a better-looking stone
    private class StonePanel extends JPanel {
        private Color color;
        
        public StonePanel(Color color) {
            this.color = color;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.fillOval(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.BLACK);
            g2d.drawOval(0, 0, getWidth()-1, getHeight()-1);
        }
    }
    
    public static void main(String[] args) {
        String[] names = {"Player 1", "CPU"};
        Color[] colors = {new Color(30, 144, 255), new Color(220, 20, 60)};
        boolean[] human = {true, false};
        SwingUtilities.invokeLater(() -> new KiviGameplay(2, names, colors, human, 30));
    }
}
