package thearcher;

import java.util.List;

/**
 * Interface representing movement behavior for units (e.g. Enemies along path).
 */
interface Movable {
    void move();
    double getSpeed();
    boolean hasReachedEnd();
}

/**
 * Interface representing attacking capability for units (e.g. Towers).
 */
interface Attacker {
    void attack(List<Enemy> enemies, List<Projectile> projectiles);
}

/**
 * Interface for units/towers that can be upgraded.
 */
interface Upgradable {
    boolean upgrade();
    int getUpgradeCost();
    int getLevel();
    int getMaxLevel();
    int getSellValue();
}
