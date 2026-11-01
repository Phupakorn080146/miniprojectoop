package thearcher;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;

/**
 * Main application window.
 * Shows MenuPanel first; switches to GamePanel + ControlPanel when the player clicks Play.
 */
public class MainFrame extends JFrame {

    private static final String SCREEN_MENU = "MENU";
    private static final String SCREEN_GAME = "GAME";

    private final CardLayout cardLayout;
    private final JPanel     rootPanel;

    private MenuPanel    menuPanel;
    private GamePanel    gamePanel;
    private ControlPanel controlPanel;
    private JPanel       gameScreen;  // Wraps gamePanel + controlPanel
    private Timer        gameTimer;

    public MainFrame() {
        setTitle("The Archer - Tower Defense Mini Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        rootPanel  = new JPanel(cardLayout);

        // --- Menu screen ---
        menuPanel = new MenuPanel(this);
        rootPanel.add(menuPanel, SCREEN_MENU);

        // --- Game screen (built lazily in startGame()) ---
        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Called by MenuPanel when the player clicks "PLAY GAME".
     * Lazily builds the game components, switches card, and starts the timer.
     */
    public void startGame() {
        menuPanel.stopAnimation();

        if (gamePanel == null) {
            gamePanel    = new GamePanel();
            controlPanel = new ControlPanel(gamePanel);
            gamePanel.setControlPanel(controlPanel);

            gameScreen = new JPanel(new BorderLayout());
            gameScreen.add(gamePanel,    BorderLayout.CENTER);
            gameScreen.add(controlPanel, BorderLayout.EAST);
            rootPanel.add(gameScreen, SCREEN_GAME);

            // 60 FPS Game Loop Timer (~16ms per tick)
            gameTimer = new Timer(16, e -> gamePanel.gameLoopTick());
        }

        cardLayout.show(rootPanel, SCREEN_GAME);
        pack();
        setLocationRelativeTo(null);
        gameTimer.start();
        gamePanel.requestFocusInWindow();
    }
}
