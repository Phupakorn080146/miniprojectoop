package thearcher;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages wave state, enemy spawning queue, and wave progression.
 */
public class WaveManager {

    private List<Point> waypoints;
    private int currentLevel;
    private int currentWave;
    private int maxWave;
    private boolean waveInProgress;
    private List<String> spawnQueue;
    private int spawnTimer;
    private int spawnInterval; // Ticks between enemy spawns

    public WaveManager(List<Point> waypoints) {
        this.waypoints = waypoints;
        this.currentLevel = 1;
        this.currentWave = 0;
        this.maxWave = 3;
        this.waveInProgress = false;
        this.spawnQueue = new ArrayList<>();
        this.spawnTimer = 0;
        this.spawnInterval = 45; // Spawn enemy every 0.75s @ 60 FPS
    }

    public void setWaypoints(List<Point> waypoints) {
        this.waypoints = waypoints;
    }

    public void resetForLevel(int level) {
        this.currentLevel = level;
        this.currentWave = 0;
        this.maxWave = 3;
        this.waveInProgress = false;
        this.spawnQueue.clear();
        this.spawnTimer = 0;
    }

    public int getCurrentLevel() { return currentLevel; }
    public int getCurrentWave() { return currentWave; }
    public int getMaxWave() { return maxWave; }
    public boolean isWaveInProgress() { return waveInProgress; }
    public boolean isCompletedAllWaves() { return currentWave >= maxWave && spawnQueue.isEmpty() && !waveInProgress; }

    public void startNextWave() {
        if (waveInProgress || currentWave >= maxWave) return;

        currentWave++;
        waveInProgress = true;
        spawnQueue.clear();

        // Build spawn queue based on Level and Wave
        if (currentLevel == 1) {
            switch (currentWave) {
                case 1:
                    for (int i = 0; i < 4; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 2; i++) spawnQueue.add("THIEF");
                    break;
                case 2:
                    for (int i = 0; i < 6; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 3; i++) spawnQueue.add("THIEF");
                    break;
                case 3:
                    for (int i = 0; i < 5; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 3; i++) spawnQueue.add("THIEF");
                    for (int i = 0; i < 2; i++) spawnQueue.add("SHIELD");
                    break;
            }
        } else if (currentLevel == 2) {
            switch (currentWave) {
                case 1:
                    for (int i = 0; i < 5; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 4; i++) spawnQueue.add("THIEF");
                    for (int i = 0; i < 2; i++) spawnQueue.add("SHIELD");
                    break;
                case 2:
                    for (int i = 0; i < 6; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 5; i++) spawnQueue.add("THIEF");
                    for (int i = 0; i < 4; i++) spawnQueue.add("SHIELD");
                    break;
                case 3:
                    for (int i = 0; i < 6; i++) spawnQueue.add("THIEF");
                    for (int i = 0; i < 5; i++) spawnQueue.add("SHIELD");
                    spawnQueue.add("BOSS");
                    break;
            }
        } else if (currentLevel == 3) {
            switch (currentWave) {
                case 1:
                    for (int i = 0; i < 6; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 5; i++) spawnQueue.add("SHIELD");
                    break;
                case 2:
                    for (int i = 0; i < 8; i++) spawnQueue.add("THIEF");
                    for (int i = 0; i < 6; i++) spawnQueue.add("SHIELD");
                    break;
                case 3:
                    for (int i = 0; i < 8; i++) spawnQueue.add("SWORDSMAN");
                    for (int i = 0; i < 6; i++) spawnQueue.add("SHIELD");
                    spawnQueue.add("BOSS");
                    spawnQueue.add("BOSS");
                    break;
            }
        }
    }

    public void update(List<Enemy> activeEnemies) {
        if (!waveInProgress) return;

        if (!spawnQueue.isEmpty()) {
            spawnTimer++;
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0;
                String type = spawnQueue.remove(0);
                Enemy enemy = createEnemy(type);
                if (enemy != null) {
                    activeEnemies.add(enemy);
                }
            }
        } else if (activeEnemies.isEmpty()) {
            // Wave cleared!
            waveInProgress = false;
        }
    }

    private Enemy createEnemy(String type) {
        switch (type) {
            case "SWORDSMAN":
                return new SwordsmanEnemy(waypoints);
            case "THIEF":
                return new ThiefEnemy(waypoints);
            case "SHIELD":
                return new ShieldEnemy(waypoints);
            case "BOSS":
                return new BossEnemy(waypoints);
            default:
                return null;
        }
    }
}
