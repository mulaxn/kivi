import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Main {
    public Main() {}

    public static void main(String[] args) {
        // Set up frame 
        JFrame frame = new JFrame("KIVI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        frame.add(mainPanel, BorderLayout.CENTER);

        Border panelBorder = BorderFactory.createLineBorder(Color.black, 2);

        // ---- Top Panel (Title and Buttons) ----
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(3, 1));
        topPanel.setBorder(panelBorder);
        mainPanel.add(topPanel);

        // Title and Logo Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel topLabel = new JLabel("Kivi by Group 3");
        ImageIcon logoIcon = new ImageIcon(new ImageIcon("Kivi.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        JLabel kiviLogo = new JLabel(logoIcon);
        titlePanel.add(topLabel);
        titlePanel.add(kiviLogo);
        topPanel.add(titlePanel);

        // Load Game and Multiplayer Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loadGame = new JButton("Load Game");
        JButton onlineMultiplayer = new JButton("Online Multiplayer");
        buttonPanel.add(loadGame);
        buttonPanel.add(onlineMultiplayer);
        topPanel.add(buttonPanel);

        // Settings Panel
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel settingsLabel = new JLabel("Settings");
        JButton timerButton = new JButton("Timer");
        JButton instructionButton = new JButton("Instruction Moral");
        settingsPanel.add(settingsLabel);
        settingsPanel.add(timerButton);
        settingsPanel.add(instructionButton);
        topPanel.add(settingsPanel);

        // ---- Center Panel (Player Selection) ----
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(panelBorder);
        mainPanel.add(centerPanel);

        // Start Game Button
        JPanel startGamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton startGameButton = new JButton("Start New Game");
        startGamePanel.add(startGameButton);
        centerPanel.add(startGamePanel, BorderLayout.NORTH);

        // Player Selection
        JPanel playerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 1; i <= 4; i++) {
            JPanel playerBox = new JPanel(new GridLayout(4, 1));
            JLabel playerLabel = new JLabel("P" + i, SwingConstants.CENTER);
            JCheckBox humanCheck = new JCheckBox("Human");
            JCheckBox cpuCheck = new JCheckBox("CPU");
            ImageIcon playerIcon = new ImageIcon(new ImageIcon("playerLogo.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            JLabel playerLogo = new JLabel(playerIcon);

            playerBox.add(playerLogo);
            playerBox.add(playerLabel);
            playerBox.add(humanCheck);
            playerBox.add(cpuCheck);
            playerPanel.add(playerBox);
        }
        centerPanel.add(playerPanel, BorderLayout.CENTER);

        // ---- Bottom Panel (Credits and Exit) ----
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.setBorder(panelBorder);
        mainPanel.add(bottomPanel);

        // Credits Button
        JPanel creditsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton creditsButton = new JButton("Credits");
        creditsPanel.add(creditsButton);
        bottomPanel.add(creditsPanel);

        // Exit Button
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton exitButton = new JButton("Exit Game");
        exitPanel.add(exitButton);
        bottomPanel.add(exitPanel);

        // Make frame visible
        frame.setVisible(true);
    }
}
