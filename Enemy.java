package thearcher;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Abstract class representing enemy units invading the fortress.
 * Implements Movable interface.
 */
public abstract class Enemy extends Unit implements Movable {
    protected int maxHp;
    protected int currentHp;
    protected double speed;
    protected int castleDamage;
    protected int rewardGold;
    
    protected List<Point> waypoints;
    protected int currentWaypointIndex;
    protected boolean reachedEnd;

    public Enemy(List<Point> waypoints, String name, int maxHp, double speed, int castleDamage, int rewardGold) {
        super(waypoints.get(0).x, waypoints.get(0).y, name);
        this.waypoints = waypoints;
        this.currentWaypointIndex = 1;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.speed = speed;
        this.castleDamage = castleDamage;
        this.rewardGold = rewardGold;
        this.reachedEnd = false;
    }

    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getCastleDamage() { return castleDamage; }
    public int getRewardGold() { return rewardGold; }
    public boolean isDead() { return currentHp <= 0; }

    @Override
    public double getSpeed() { return speed; }

    @Override
    public boolean hasReachedEnd() { return reachedEnd; }

    public void takeDamage(int damage) {
        this.currentHp -= damage;
        if (this.currentHp <= 0) {
            this.currentHp = 0;
            this.active = false;
        }
    }

    @Override
    public void move() {
        if (reachedEnd || !active) return;

        if (currentWaypointIndex >= waypoints.size()) {
            reachedEnd = true;
            active = false;
            return;
        }

        Point target = waypoints.get(currentWaypointIndex);
        double dx = target.x - x;
        double dy = target.y - y;
        double dist = Math.hypot(dx, dy);

        if (dist <= speed) {
            x = target.x;
            y = target.y;
            currentWaypointIndex++;
        } else {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }
    }

    @Override
    public void update() {
        move();
    }

    /**
     * Helper to render generic health bar above enemy.
     */
    protected void renderHealthBar(Graphics2D g, int width, int height, int yOffset) {
        int barX = (int) x - width / 2;
        int barY = (int) y - yOffset;

        // Background (Red)
        g.setColor(Color.RED);
        g.fillRect(barX, barY, width, height);

        // Current HP (Green)
        int greenWidth = (int) ((double) currentHp / maxHp * width);
        g.setColor(Color.GREEN);
        g.fillRect(barX, barY, greenWidth, height);

        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, width, height);
    }
}

/**
 * Swordsman Enemy — animated walk using 7-frame sprite sheet (swordsman_walk.png)
 */
class SwordsmanEnemy extends Enemy {
    private static final int FRAME_COUNT = 7;
    private static final int TICKS_PER_FRAME = 7;

    private int frameTick  = 0;
    private int frameIndex = 0;
    private boolean facingLeft = false;

    public SwordsmanEnemy(List<Point> waypoints) {
        super(waypoints, "Swordsman", 150, 1.5, 10, 25);
    }

    @Override
    public void update() {
        double prevX = x;
        super.update();

        double dx = x - prevX;
        if (dx < -0.1)      facingLeft = true;
        else if (dx > 0.1)  facingLeft = false;

        if (active) {
            frameTick++;
            if (frameTick >= TICKS_PER_FRAME) {
                frameTick  = 0;
                frameIndex = (frameIndex + 1) % FRAME_COUNT;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage[] frames =
                ImageLoader.getFramesFromFolder("swordsman_walk", "swordsman_frame_", FRAME_COUNT, true);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (frames != null && frames[frameIndex] != null) {
            java.awt.image.BufferedImage frame = frames[frameIndex];

            int drawH = 40;
            int drawW = (int) Math.round((double) frame.getWidth() / frame.getHeight() * drawH);

            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;

            if (facingLeft) {
                g.drawImage(frame, px + drawW, py, -drawW, drawH, null);
            } else {
                g.drawImage(frame, px, py, drawW, drawH, null);
            }
            renderHealthBar(g, Math.max(drawW - 4, 24), 4, drawH / 2 + 6);
        } else {
            // Fallback
            int drawW = 46, drawH = 46;
            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;
            g.setColor(new Color(178, 34, 34));
            g.fillRect(px, py, drawW, drawH);
            renderHealthBar(g, 26, 4, 18);
        }
    }
}


/**
 * Thief Enemy — animated walk using 7-frame sprite sheet (thief_walk.png)
 */
class ThiefEnemy extends Enemy {
    private static final int FRAME_COUNT = 7;
    /** How many game-update ticks to display each frame (lower = faster). */
    private static final int TICKS_PER_FRAME = 6;

    private int frameTick  = 0;
    private int frameIndex = 0;
    /** true = sprite should face left (flip horizontally) */
    private boolean facingLeft = false;

    public ThiefEnemy(List<Point> waypoints) {
        super(waypoints, "Thief", 80, 2.8, 10, 20);
    }

    @Override
    public void update() {
        double prevX = x;
        super.update();   // move() is called here — updates x, y

        // Track horizontal direction for sprite flipping
        double dx = x - prevX;
        if (dx < -0.1)       facingLeft = true;
        else if (dx > 0.1)   facingLeft = false;

        // Advance animation only while alive and still moving
        if (active) {
            frameTick++;
            if (frameTick >= TICKS_PER_FRAME) {
                frameTick  = 0;
                frameIndex = (frameIndex + 1) % FRAME_COUNT;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage[] frames =
                ImageLoader.getFramesFromFolder("thief_walk", "thief_walk_", FRAME_COUNT, true);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (frames != null && frames[frameIndex] != null) {
            java.awt.image.BufferedImage frame = frames[frameIndex];

            int drawH = 34;
            int drawW = (int) Math.round((double) frame.getWidth() / frame.getHeight() * drawH);

            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;

            if (facingLeft) {
                // Flip horizontally: draw from right edge with negative width
                g.drawImage(frame, px + drawW, py, -drawW, drawH, null);
            } else {
                g.drawImage(frame, px, py, drawW, drawH, null);
            }
            renderHealthBar(g, Math.max(drawW - 4, 22), 4, drawH / 2 + 6);
        } else {
            // Fallback: plain coloured rectangle
            int drawW = 36, drawH = 36;
            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;
            g.setColor(new Color(45, 45, 60));
            g.fillRect(px, py, drawW, drawH);
            renderHealthBar(g, 26, 4, 24);
        }
    }
}

/**
 * Shield Enemy — animated walk using 7-frame sprite sheet (shield_walk.png)
 */
class ShieldEnemy extends Enemy {
    private static final int FRAME_COUNT = 7;
    private static final int TICKS_PER_FRAME = 8;

    private int frameTick  = 0;
    private int frameIndex = 0;
    private boolean facingLeft = false;

    public ShieldEnemy(List<Point> waypoints) {
        super(waypoints, "Shield Soldier", 300, 0.9, 20, 50);
    }

    @Override
    public void update() {
        double prevX = x;
        super.update();

        double dx = x - prevX;
        if (dx < -0.1)      facingLeft = true;
        else if (dx > 0.1)  facingLeft = false;

        if (active) {
            frameTick++;
            if (frameTick >= TICKS_PER_FRAME) {
                frameTick  = 0;
                frameIndex = (frameIndex + 1) % FRAME_COUNT;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage[] frames =
                ImageLoader.getFramesFromFolder("shield_walk", "shield_walk_", FRAME_COUNT, true);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (frames != null && frames[frameIndex] != null) {
            java.awt.image.BufferedImage frame = frames[frameIndex];

            int drawH = 40;
            int drawW = (int) Math.round((double) frame.getWidth() / frame.getHeight() * drawH);
            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;

            if (facingLeft) {
                g.drawImage(frame, px + drawW, py, -drawW, drawH, null);
            } else {
                g.drawImage(frame, px, py, drawW, drawH, null);
            }
            renderHealthBar(g, Math.max(drawW - 4, 28), 5, drawH / 2 + 7);
        } else {
            int drawW = 46, drawH = 46;
            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;
            g.setColor(new Color(70, 130, 180));
            g.fillRect(px, py, drawW, drawH);
            renderHealthBar(g, 30, 5, 20);
        }
    }
}


/**
 * Boss Enemy (Sword & Shield) — animated walk using 7-frame sprite sheet (boss_walk.png)
 */
class BossEnemy extends Enemy {
    private static final int FRAME_COUNT = 7;
    private static final int TICKS_PER_FRAME = 7;

    private int frameTick  = 0;
    private int frameIndex = 0;
    private boolean facingLeft = false;

    public BossEnemy(List<Point> waypoints) {
        super(waypoints, "BOSS (Sword & Shield)", 1000, 1.2, 50, 200);
    }

    @Override
    public void update() {
        double prevX = x;
        super.update();

        double dx = x - prevX;
        if (dx < -0.1)      facingLeft = true;
        else if (dx > 0.1)  facingLeft = false;

        if (active) {
            frameTick++;
            if (frameTick >= TICKS_PER_FRAME) {
                frameTick  = 0;
                frameIndex = (frameIndex + 1) % FRAME_COUNT;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        java.awt.image.BufferedImage[] frames =
                ImageLoader.getFramesFromFolder("boss_walk", "boss_walk_", FRAME_COUNT, true);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (frames != null && frames[frameIndex] != null) {
            java.awt.image.BufferedImage frame = frames[frameIndex];

            int drawH = 75;
            int drawW = (int) Math.round((double) frame.getWidth() / frame.getHeight() * drawH);
            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;

            // Dark Crimson Aura Glow
            g.setColor(new Color(220, 20, 60, 70));
            g.fillOval((int) x - drawW / 2 - 4, (int) y - drawH / 2 - 4, drawW + 8, drawH + 8);

            if (facingLeft) {
                g.drawImage(frame, px + drawW, py, -drawW, drawH, null);
            } else {
                g.drawImage(frame, px, py, drawW, drawH, null);
            }
            renderHealthBar(g, Math.max(drawW, 38), 6, drawH / 2 + 9);
        } else {
            int drawW = 50, drawH = 50;
            int px = (int) x - drawW / 2;
            int py = (int) y - drawH / 2;
            g.setColor(new Color(220, 20, 60, 70));
            g.fillOval((int) x - 28, (int) y - 28, 56, 56);
            g.setColor(new Color(30, 30, 35));
            g.fillRect(px, py, drawW, drawH);
            renderHealthBar(g, 42, 6, 30);
        }
    }
}
