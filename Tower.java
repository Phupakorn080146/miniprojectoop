package thearcher;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Abstract class representing defensive towers (Player Units).
 * Implements Attacker and Upgradable interfaces.
 */
public abstract class Tower extends Unit implements Attacker, Upgradable {
    protected int gridX;
    protected int gridY;
    protected int damage;
    protected double range; // In pixels
    protected int level;
    protected int maxLevel;
    protected int cost;
    protected int attackCooldown;
    protected int attackTimer;
    protected double buffMultiplier; // Temporary buff multiplier (e.g. from Priest)

    public Tower(int gridX, int gridY, int tileSize, String name, int damage, double rangeTiles, int cost, int attackCooldown) {
        super(gridX * tileSize + tileSize / 2.0, gridY * tileSize + tileSize / 2.0, name);
        this.gridX = gridX;
        this.gridY = gridY;
        this.damage = damage;
        this.range = rangeTiles * tileSize;
        this.cost = cost;
        this.level = 1;
        this.maxLevel = 3;
        this.attackCooldown = attackCooldown;
        this.attackTimer = 0;
        this.buffMultiplier = 1.0;
    }

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public int getDamage() { return (int) (damage * buffMultiplier); }
    public int getBaseDamage() { return damage; }
    public double getRange() { return range; }
    public int getCost() { return cost; }
    
    @Override
    public int getLevel() { return level; }

    @Override
    public int getMaxLevel() { return maxLevel; }

    @Override
    public int getUpgradeCost() {
        return (int) (cost * 0.75 * level);
    }

    @Override
    public int getSellValue() {
        return (int) (cost * 0.6 * level);
    }

    public void setBuffMultiplier(double buffMultiplier) {
        this.buffMultiplier = buffMultiplier;
    }

    @Override
    public boolean upgrade() {
        if (level < maxLevel) {
            level++;
            this.damage = (int) (this.damage * 1.35);
            this.range += 15;
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        if (attackTimer > 0) {
            attackTimer--;
        }
    }

    protected boolean canAttack() {
        return attackTimer <= 0;
    }

    protected void resetCooldown() {
        attackTimer = attackCooldown;
    }

    /**
     * Draw tower range circle (when selected).
     */
    public void renderRange(Graphics2D g) {
        int r = (int) range;
        g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
    }
}

/**
 * Archer Tower
 */
class ArcherTower extends Tower {
    public ArcherTower(int gridX, int gridY, int tileSize) {
        super(gridX, gridY, tileSize, "Archer", 25, 5.0, 100, 30);
    }

    @Override
    public void attack(List<Enemy> enemies, List<Projectile> projectiles) {
        if (!canAttack()) return;

        Enemy target = null;
        double maxDist = range;

        for (Enemy enemy : enemies) {
            if (enemy.isActive() && !enemy.isDead()) {
                double dist = distanceTo(enemy);
                if (dist <= maxDist) {
                    target = enemy;
                    break;
                }
            }
        }

        if (target != null) {
            projectiles.add(new Projectile(x, y, target, getDamage(), Projectile.Type.ARROW, 12.0, 0));
            resetCooldown();
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage img = ImageLoader.getImage("images/archer.png", true);
        int drawW = 46;
        int drawH = 46;
        int px = (int) x - drawW / 2;
        int py = (int) y - drawH / 2;

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (img != null) {
            g.drawImage(img, px, py, drawW, drawH, null);
        } else {
            g.setColor(new Color(34, 139, 34));
            g.fillRect(px, py, drawW, drawH);
        }

        // Level Badge
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect((int) x - 12, (int) y + 14, 24, 12);
        g.setColor(Color.YELLOW);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10));
        g.drawString("L" + level, (int) x - 6, (int) y + 23);
    }
}

/**
 * Mage Tower
 */
class MageTower extends Tower {
    private final double splashRadius = 50.0;

    public MageTower(int gridX, int gridY, int tileSize) {
        super(gridX, gridY, tileSize, "Mage", 40, 4.0, 150, 45);
    }

    @Override
    public void attack(List<Enemy> enemies, List<Projectile> projectiles) {
        if (!canAttack()) return;

        Enemy target = null;
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && !enemy.isDead() && distanceTo(enemy) <= range) {
                target = enemy;
                break;
            }
        }

        if (target != null) {
            projectiles.add(new Projectile(x, y, target, getDamage(), Projectile.Type.MAGIC, 9.0, splashRadius));
            resetCooldown();
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage img = ImageLoader.getImage("images/mage.png", true);
        int drawW = 46;
        int drawH = 46;
        int px = (int) x - drawW / 2;
        int py = (int) y - drawH / 2;

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (img != null) {
            g.drawImage(img, px, py, drawW, drawH, null);
        } else {
            g.setColor(new Color(30, 90, 185));
            g.fillRect(px, py, drawW, drawH);
        }

        // Level Badge
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect((int) x - 12, (int) y + 14, 24, 12);
        g.setColor(Color.YELLOW);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10));
        g.drawString("L" + level, (int) x - 6, (int) y + 23);
    }
}

/**
 * Priest Tower
 */
class PriestTower extends Tower {
    private double buffRange;

    public PriestTower(int gridX, int gridY, int tileSize) {
        super(gridX, gridY, tileSize, "Priest", 10, 4.0, 120, 35);
        this.buffRange = range;
    }

    public void applyBuffToTowers(List<Tower> towers) {
        for (Tower tower : towers) {
            if (tower != this && distanceTo(tower) <= buffRange) {
                tower.setBuffMultiplier(1.25 + (0.05 * (level - 1)));
            }
        }
    }

    @Override
    public void attack(List<Enemy> enemies, List<Projectile> projectiles) {
        if (!canAttack()) return;

        Enemy target = null;
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && !enemy.isDead() && distanceTo(enemy) <= range) {
                target = enemy;
                break;
            }
        }

        if (target != null) {
            projectiles.add(new Projectile(x, y, target, getDamage(), Projectile.Type.HOLY, 10.0, 0));
            resetCooldown();
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage img = ImageLoader.getImage("images/priest.png", true);
        int drawW = 46;
        int drawH = 46;
        int px = (int) x - drawW / 2;
        int py = (int) y - drawH / 2;

        // Golden Aura
        g.setColor(new Color(255, 215, 0, 35));
        int auraR = (int) buffRange;
        g.fillOval((int) x - auraR, (int) y - auraR, auraR * 2, auraR * 2);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (img != null) {
            g.drawImage(img, px, py, drawW, drawH, null);
        } else {
            g.setColor(new Color(218, 165, 32));
            g.fillRect(px, py, drawW, drawH);
        }

        // Level Badge
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect((int) x - 12, (int) y + 14, 24, 12);
        g.setColor(Color.YELLOW);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10));
        g.drawString("L" + level, (int) x - 6, (int) y + 23);
    }
}
