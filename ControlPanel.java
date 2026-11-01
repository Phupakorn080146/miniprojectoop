package thearcher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Control sidebar panel providing user controls, status displays, and tower upgrades.
 */
public class ControlPanel extends JPanel {

    private GamePanel gamePanel;

    private JLabel stageLabel;
    private JLabel hpLabel;
    private JProgressBar hpBar;
    private JLabel goldLabel;
    private JLabel waveLabel;

    private JButton btnArcher;
    private JButton btnMage;
    private JButton btnPriest;

    private JLabel selectedTowerLabel;
    private JLabel towerDetailsLabel;
    private JButton btnUpgrade;
    private JButton btnSell;

    private JButton btnStartWave;

    public ControlPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setPreferredSize(new Dimension(280, 500));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        initStatusSection();
        initBuildSection();
        initUpgradeSection();
        initWaveSection();

        updateStatus();
    }

    private void initStatusSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Fortress Status", 0, 0, new Font("SansSerif", Font.BOLD, 14), Color.YELLOW));

        stageLabel = new JLabel("Stage: 1 / 3");
        stageLabel.setForeground(Color.ORANGE);
        stageLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        hpLabel = new JLabel("Castle HP: 100 / 100");
        hpLabel.setForeground(Color.WHITE);
        hpLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        hpBar = new JProgressBar(0, 100);
        hpBar.setValue(100);
        hpBar.setForeground(Color.GREEN);
        hpBar.setBackground(Color.RED);

        goldLabel = new JLabel("Gold: 300 G");
        goldLabel.setForeground(new Color(255, 215, 0));
        goldLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        waveLabel = new JLabel("Wave: 0 / 3");
        waveLabel.setForeground(Color.CYAN);
        waveLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        panel.add(stageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(hpLabel);
        panel.add(hpBar);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(goldLabel);
        panel.add(waveLabel);

        add(panel);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void initBuildSection() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 6, 6));
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Hire Defender  (max 3 each)", 0, 0, new Font("SansSerif", Font.BOLD, 14), Color.GREEN));

        // Button text is updated dynamically in updateStatus()
        btnArcher = new JButton("<html><b>Archer</b> (100G) &nbsp;<small>[0/3]</small><br><small>Single Target, Long Range</small></html>");
        btnMage   = new JButton("<html><b>Mage</b> (150G) &nbsp;<small>[0/3]</small><br><small>AoE Splash Damage</small></html>");
        btnPriest = new JButton("<html><b>Priest</b> (120G) &nbsp;<small>[0/3]</small><br><small>Boost Ally Damage</small></html>");

        btnArcher.addActionListener(e -> gamePanel.setSelectedBuildType("ARCHER"));
        btnMage.addActionListener(e -> gamePanel.setSelectedBuildType("MAGE"));
        btnPriest.addActionListener(e -> gamePanel.setSelectedBuildType("PRIEST"));

        panel.add(btnArcher);
        panel.add(btnMage);
        panel.add(btnPriest);

        add(panel);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void initUpgradeSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Tower Controls", 0, 0, new Font("SansSerif", Font.BOLD, 14), Color.ORANGE));

        selectedTowerLabel = new JLabel("Selected: None");
        selectedTowerLabel.setForeground(Color.WHITE);

        towerDetailsLabel = new JLabel("Click a tower on map");
        towerDetailsLabel.setForeground(Color.LIGHT_GRAY);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        btnPanel.setBackground(new Color(45, 45, 45));

        btnUpgrade = new JButton("Upgrade");
        btnSell = new JButton("Sell");

        btnUpgrade.addActionListener(e -> gamePanel.upgradeSelectedTower());
        btnSell.addActionListener(e -> gamePanel.sellSelectedTower());

        btnPanel.add(btnUpgrade);
        btnPanel.add(btnSell);

        panel.add(selectedTowerLabel);
        panel.add(towerDetailsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(btnPanel);

        add(panel);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void initWaveSection() {
        btnStartWave = new JButton("START NEXT WAVE");
        btnStartWave.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStartWave.setBackground(new Color(46, 139, 87));
        btnStartWave.setForeground(Color.WHITE);
        btnStartWave.setPreferredSize(new Dimension(250, 45));

        btnStartWave.addActionListener(e -> {
            if (gamePanel.isLevelCleared()) {
                gamePanel.advanceToNextLevel();
            } else {
                gamePanel.startNextWave();
            }
        });

        add(btnStartWave);
    }

    public void updateStatus() {
        stageLabel.setText("Stage: " + gamePanel.getCurrentLevel() + " / " + gamePanel.getMaxLevel());

        int hp = gamePanel.getCastleHp();
        int maxHp = gamePanel.getMaxCastleHp();
        hpLabel.setText("Castle HP: " + hp + " / " + maxHp);
        hpBar.setValue((int) ((double) hp / maxHp * 100));

        goldLabel.setText("Gold: " + gamePanel.getGold() + " G");

        WaveManager wm = gamePanel.getWaveManager();
        waveLabel.setText("Wave: " + wm.getCurrentWave() + " / " + wm.getMaxWave());

        if (gamePanel.isLevelCleared()) {
            btnStartWave.setText("NEXT STAGE");
            btnStartWave.setBackground(new Color(30, 144, 255));
            btnStartWave.setEnabled(true);
        } else {
            btnStartWave.setText("START NEXT WAVE");
            btnStartWave.setBackground(new Color(46, 139, 87));
            btnStartWave.setEnabled(!wm.isWaveInProgress() && !gamePanel.isGameOver() && !gamePanel.isVictory());
        }

        // --- Per-type defender counters: update button text + enable/disable ---
        int maxPer  = gamePanel.getMaxPerType();
        boolean notOver = !gamePanel.isGameOver() && !gamePanel.isVictory();
        int gold = gamePanel.getGold();

        int archerCnt = gamePanel.countTowersByType(ArcherTower.class);
        int mageCnt   = gamePanel.countTowersByType(MageTower.class);
        int priestCnt = gamePanel.countTowersByType(PriestTower.class);

        btnArcher.setText("<html><b>Archer</b> (100G)<br><small>Single Target, Long Range</small></html>");
        btnMage.setText("<html><b>Mage</b> (150G)<br><small>AoE Splash Damage</small></html>");
        btnPriest.setText("<html><b>Priest</b> (120G)<br><small>Boost Ally Damage</small></html>");

        btnArcher.setEnabled(notOver && archerCnt < maxPer && gold >= 100);
        btnMage.setEnabled(notOver   && mageCnt   < maxPer && gold >= 150);
        btnPriest.setEnabled(notOver && priestCnt < maxPer && gold >= 120);

        Tower selected = gamePanel.getSelectedTower();
        if (selected != null) {
            selectedTowerLabel.setText(selected.getName() + " (Lvl " + selected.getLevel() + ")");
            towerDetailsLabel.setText("<html>Dmg: " + selected.getDamage() + "<br>Cost Upg: " + selected.getUpgradeCost() + "G</html>");
            btnUpgrade.setEnabled(selected.getLevel() < selected.getMaxLevel() && gamePanel.getGold() >= selected.getUpgradeCost());
            btnSell.setEnabled(true);
        } else {
            selectedTowerLabel.setText("Selected: None");
            towerDetailsLabel.setText("Click a tower on map");
            btnUpgrade.setEnabled(false);
            btnSell.setEnabled(false);
        }
    }
}
