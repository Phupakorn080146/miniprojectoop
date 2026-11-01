package thearcher;

import javax.swing.SwingUtilities;

/**
 * Entry point for The Archer game application.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame window = new MainFrame();
            window.setVisible(true);
        });
    }
}

// java -cp d:\oop\mini_project thearcher.Main  