package thearcher;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game panel rendering map, units, projectiles, and handling mouse
 * interaction.
 */
public class GamePanel extends JPanel {

    private final int cols = 14;
    private final int rows = 10;
    private final int tileSize = 50; // 700 x 500 px play area

    private int currentLevel;
    private final int maxLevel = 3;

    private MapTile[][] grid;
    private List<Point> waypoints;
    private List<Tower> towers;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;

    private WaveManager waveManager;
    private int castleHp;
    private int maxCastleHp;
    private int gold;

    private static final int MAX_PER_TYPE = 3; // Max defenders per type (Archer/Mage/Priest)

    private String selectedBuildType; // "ARCHER", "MAGE", "PRIEST", or null
    private Tower selectedTower; // Tower currently selected by mouse click

    private boolean gameOver;
    private boolean victory;
    private boolean levelCleared;

    private ControlPanel controlPanel; // Reference for UI sync

    public GamePanel() {
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        setBackground(Color.BLACK);

        towers = new ArrayList<>();
        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();

        currentLevel = 1;
        maxCastleHp = 100;
        castleHp = maxCastleHp;
        gold = 300; // Initial gold

        waypoints = new ArrayList<>();
        waveManager = new WaveManager(waypoints);

        loadLevel(currentLevel);

        selectedBuildType = null;
        selectedTower = null;
        gameOver = false;
        victory = false;
        levelCleared = false;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    public void setControlPanel(ControlPanel cp) {
        this.controlPanel = cp;
    }

    public int getCastleHp() {
        return castleHp;
    }

    public int getMaxCastleHp() {
        return maxCastleHp;
    }

    public int getGold() {
        return gold;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public Tower getSelectedTower() {
        return selectedTower;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isVictory() {
        return victory;
    }

    /** Count how many towers of a given class are on the map. */
    public int countTowersByType(Class<?> type) {
        int count = 0;
        for (Tower t : towers) {
            if (t.getClass() == type) count++;
        }
        return count;
    }

    /** Returns the per-type tower cap. */
    public int getMaxPerType() {
        return MAX_PER_TYPE;
    }

    // Legacy helpers kept for ControlPanel compatibility
    public int getTowerCount() { return towers.size(); }
    public int getMaxTowers()  { return MAX_PER_TYPE * 3; } // total possible

    public void setSelectedBuildType(String type) {
        this.selectedBuildType = type;
        this.selectedTower = null; // Clear selected existing tower
        repaint();
    }

    public void loadLevel(int level) {
        currentLevel = level;
        towers.clear();
        enemies.clear();
        projectiles.clear();
        gold = 300; // Reset gold per level
        castleHp = maxCastleHp; // Reset castle HP per level
        selectedTower = null;
        selectedBuildType = null;
        levelCleared = false;

        initMapForLevel(level);
        waveManager.setWaypoints(waypoints);
        waveManager.resetForLevel(level);
    }

    public void advanceToNextLevel() {
        if (currentLevel < maxLevel) {
            loadLevel(currentLevel + 1);
            if (controlPanel != null) controlPanel.updateStatus();
            repaint();
        }
    }

    public boolean isLevelCleared() {
        return levelCleared;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    private void initMapForLevel(int level) {
        grid = new MapTile[rows][cols];

        // Default all to Grass
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new MapTile(c, r, tileSize, MapTile.Type.GRASS);
            }
        }

        waypoints.clear();
        int half = tileSize / 2;

        if (level == 1) {
            // Level 1 Path (S-Curve path)
            // Row 2: (0,2) to (10,2)
            for (int c = 0; c <= 10; c++) grid[2][c] = new MapTile(c, 2, tileSize, MapTile.Type.PATH);
            // Col 10: (10,2) to (10,6)
            for (int r = 2; r <= 6; r++) grid[r][10] = new MapTile(10, r, tileSize, MapTile.Type.PATH);
            // Row 6: (10,6) to (3,6)
            for (int c = 10; c >= 3; c--) grid[6][c] = new MapTile(c, 6, tileSize, MapTile.Type.PATH);
            // Col 3: (3,6) to (3,8)
            for (int r = 6; r <= 8; r++) grid[r][3] = new MapTile(3, r, tileSize, MapTile.Type.PATH);
            // Row 8: (3,8) to (13,8)
            for (int c = 3; c <= 13; c++) grid[8][c] = new MapTile(c, 8, tileSize, MapTile.Type.PATH);

            grid[8][13] = new MapTile(13, 8, tileSize, MapTile.Type.CASTLE);

            waypoints.add(new Point(0 * tileSize + half, 2 * tileSize + half));
            waypoints.add(new Point(10 * tileSize + half, 2 * tileSize + half));
            waypoints.add(new Point(10 * tileSize + half, 6 * tileSize + half));
            waypoints.add(new Point(3 * tileSize + half, 6 * tileSize + half));
            waypoints.add(new Point(3 * tileSize + half, 8 * tileSize + half));
            waypoints.add(new Point(13 * tileSize + half, 8 * tileSize + half));

        } else if (level == 2) {
            // Level 2 Path (Z-shape across middle)
            // Row 1: (0,1) to (12,1)
            for (int c = 0; c <= 12; c++) grid[1][c] = new MapTile(c, 1, tileSize, MapTile.Type.PATH);
            // Col 12: (12,1) to (12,5)
            for (int r = 1; r <= 5; r++) grid[r][12] = new MapTile(12, r, tileSize, MapTile.Type.PATH);
            // Row 5: (12,5) to (1,5)
            for (int c = 12; c >= 1; c--) grid[5][c] = new MapTile(c, 5, tileSize, MapTile.Type.PATH);
            // Col 1: (1,5) to (1,8)
            for (int r = 5; r <= 8; r++) grid[r][1] = new MapTile(1, r, tileSize, MapTile.Type.PATH);
            // Row 8: (1,8) to (13,8)
            for (int c = 1; c <= 13; c++) grid[8][c] = new MapTile(c, 8, tileSize, MapTile.Type.PATH);

            grid[8][13] = new MapTile(13, 8, tileSize, MapTile.Type.CASTLE);

            waypoints.add(new Point(0 * tileSize + half, 1 * tileSize + half));
            waypoints.add(new Point(12 * tileSize + half, 1 * tileSize + half));
            waypoints.add(new Point(12 * tileSize + half, 5 * tileSize + half));
            waypoints.add(new Point(1 * tileSize + half, 5 * tileSize + half));
            waypoints.add(new Point(1 * tileSize + half, 8 * tileSize + half));
            waypoints.add(new Point(13 * tileSize + half, 8 * tileSize + half));

        } else if (level == 3) {
            // Level 3 Path (Spiral Loop in center)
            // Col 2: (2,0) to (2,7)
            for (int r = 0; r <= 7; r++) grid[r][2] = new MapTile(2, r, tileSize, MapTile.Type.PATH);
            // Row 7: (2,7) to (11,7)
            for (int c = 2; c <= 11; c++) grid[7][c] = new MapTile(c, 7, tileSize, MapTile.Type.PATH);
            // Col 11: (11,7) to (11,2)
            for (int r = 7; r >= 2; r--) grid[r][11] = new MapTile(11, r, tileSize, MapTile.Type.PATH);
            // Row 2: (11,2) to (6,2)
            for (int c = 11; c >= 6; c--) grid[2][c] = new MapTile(c, 2, tileSize, MapTile.Type.PATH);
            // Col 6: (6,2) to (6,4)
            for (int r = 2; r <= 4; r++) grid[r][6] = new MapTile(6, r, tileSize, MapTile.Type.PATH);

            grid[4][6] = new MapTile(6, 4, tileSize, MapTile.Type.CASTLE);

            waypoints.add(new Point(2 * tileSize + half, 0 * tileSize + half));
            waypoints.add(new Point(2 * tileSize + half, 7 * tileSize + half));
            waypoints.add(new Point(11 * tileSize + half, 7 * tileSize + half));
            waypoints.add(new Point(11 * tileSize + half, 2 * tileSize + half));
            waypoints.add(new Point(6 * tileSize + half, 2 * tileSize + half));
            waypoints.add(new Point(6 * tileSize + half, 4 * tileSize + half));
        }
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        if (gameOver || victory)
            return;

        int gx = mouseX / tileSize;
        int gy = mouseY / tileSize;

        if (gx < 0 || gx >= cols || gy < 0 || gy >= rows)
            return;

        // Check if clicking existing tower
        Tower clickedTower = getTowerAt(gx, gy);
        if (clickedTower != null) {
            selectedTower = clickedTower;
            selectedBuildType = null;
            if (controlPanel != null)
                controlPanel.updateStatus();
            repaint();
            return;
        }

        // Deselect tower if clicking elsewhere
        selectedTower = null;

        // Try building new tower if build type selected
        if (selectedBuildType != null) {
            MapTile tile = grid[gy][gx];
            if (tile.isBuildable()) {
                // Enforce per-type limit
                Class<?> targetClass = null;
                switch (selectedBuildType) {
                    case "ARCHER":  targetClass = ArcherTower.class;  break;
                    case "MAGE":    targetClass = MageTower.class;    break;
                    case "PRIEST":  targetClass = PriestTower.class;  break;
                }
                if (targetClass != null && countTowersByType(targetClass) >= MAX_PER_TYPE) {
                    // At cap for this type — cancel selection
                    selectedBuildType = null;
                    if (controlPanel != null) controlPanel.updateStatus();
                    repaint();
                    return;
                }

                Tower newTower = null;
                switch (selectedBuildType) {
                    case "ARCHER":
                        if (gold >= 100) {
                            newTower = new ArcherTower(gx, gy, tileSize);
                            gold -= 100;
                        }
                        break;
                    case "MAGE":
                        if (gold >= 150) {
                            newTower = new MageTower(gx, gy, tileSize);
                            gold -= 150;
                        }
                        break;
                    case "PRIEST":
                        if (gold >= 120) {
                            newTower = new PriestTower(gx, gy, tileSize);
                            gold -= 120;
                        }
                        break;
                }

                if (newTower != null) {
                    towers.add(newTower);
                    tile.setOccupied(true);
                    selectedBuildType = null; // Reset selection after placement
                }
            }
        }

        if (controlPanel != null)
            controlPanel.updateStatus();
        repaint();
    }

    private Tower getTowerAt(int gx, int gy) {
        for (Tower t : towers) {
            if (t.getGridX() == gx && t.getGridY() == gy) {
                return t;
            }
        }
        return null;
    }

    public void upgradeSelectedTower() {
        if (selectedTower != null && selectedTower.getLevel() < selectedTower.getMaxLevel()) {
            int cost = selectedTower.getUpgradeCost();
            if (gold >= cost) {
                gold -= cost;
                selectedTower.upgrade();
                if (controlPanel != null)
                    controlPanel.updateStatus();
                repaint();
            }
        }
    }

    public void sellSelectedTower() {
        if (selectedTower != null) {
            gold += selectedTower.getSellValue();
            grid[selectedTower.getGridY()][selectedTower.getGridX()].setOccupied(false);
            towers.remove(selectedTower);
            selectedTower = null;
            if (controlPanel != null)
                controlPanel.updateStatus();
            repaint();
        }
    }

    public void startNextWave() {
        if (!waveManager.isWaveInProgress()) {
            waveManager.startNextWave();
            if (controlPanel != null)
                controlPanel.updateStatus();
        }
    }

    public void gameLoopTick() {
        if (gameOver || victory)
            return;

        // Reset all tower buff multipliers to 1.0
        for (Tower t : towers) {
            t.setBuffMultiplier(1.0);
        }

        // Apply Priest buffs
        for (Tower t : towers) {
            if (t instanceof PriestTower) {
                ((PriestTower) t).applyBuffToTowers(towers);
            }
        }

        // Wave Spawner Update
        waveManager.update(enemies);

        // Update Towers & Attacks
        for (Tower t : towers) {
            t.update();
            t.attack(enemies, projectiles);
        }

        // Update Projectiles
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(enemies);
            if (!p.isActive()) {
                projectiles.remove(i);
            }
        }

        // Update Enemies & Movement
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update();

            if (enemy.hasReachedEnd()) {
                castleHp -= enemy.getCastleDamage();
                enemies.remove(i);
                if (castleHp <= 0) {
                    castleHp = 0;
                    gameOver = true;
                }
            } else if (enemy.isDead()) {
                gold += enemy.getRewardGold();
                enemies.remove(i);
            }
        }

        // Check Stage Clear & Victory Condition
        if (waveManager.isCompletedAllWaves() && enemies.isEmpty()) {
            if (currentLevel < maxLevel) {
                levelCleared = true;
            } else {
                victory = true;
            }
        }

        if (controlPanel != null)
            controlPanel.updateStatus();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Render Map Grid Tiles
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].render(g2d);
            }
        }

        // 2. Render Towers
        for (Tower t : towers) {
            t.render(g2d);
        }

        // 3. Render Enemies
        for (Enemy e : enemies) {
            e.render(g2d);
        }

        // 4. Render Projectiles
        for (Projectile p : projectiles) {
            p.render(g2d);
        }

        // 5. Render Range indicator for selected tower
        if (selectedTower != null) {
            g2d.setColor(new Color(255, 255, 255, 120));
            selectedTower.renderRange(g2d);
            g2d.setColor(Color.YELLOW);
            int gx = selectedTower.getGridX() * tileSize;
            int gy = selectedTower.getGridY() * tileSize;
            g2d.drawRect(gx, gy, tileSize, tileSize);
        }

        // 6. Overlays for Game Over / Level Cleared / Final Victory
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 42));
            g2d.drawString("GAME OVER", getWidth() / 2 - 130, getHeight() / 2 - 20);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("The Castle was destroyed!", getWidth() / 2 - 120, getHeight() / 2 + 20);
        } else if (levelCleared) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
            g2d.drawString("STAGE " + currentLevel + " CLEARED!", getWidth() / 2 - 160, getHeight() / 2 - 20);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Click 'NEXT STAGE' to reset & continue!", getWidth() / 2 - 170, getHeight() / 2 + 20);
        } else if (victory) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 42));
            g2d.drawString("VICTORY!", getWidth() / 2 - 100, getHeight() / 2 - 20);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("You completed all 3 Stages!", getWidth() / 2 - 140, getHeight() / 2 + 20);
        }
    }
}
