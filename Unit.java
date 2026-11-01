package thearcher;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Abstract base class representing any entity in the game world.
 */
public abstract class Unit {
    protected double x;
    protected double y;
    protected String name;
    protected boolean active;

    public Unit(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.active = true;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double distanceTo(Unit other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.hypot(dx, dy);
    }

    public double distanceTo(double targetX, double targetY) {
        double dx = this.x - targetX;
        double dy = this.y - targetY;
        return Math.hypot(dx, dy);
    }

    /**
     * Render the unit on the canvas.
     */
    public abstract void render(Graphics2D g);

    /**
     * Update loop logic per tick.
     */
    public abstract void update();
}

/**
 * Visual projectile created when towers attack.
 */
class Projectile extends Unit {

    public enum Type { ARROW, MAGIC, HOLY }

    private Enemy target;
    private int damage;
    private Type type;
    private double speed;
    private double splashRadius;
    private double angle = 0;

    public Projectile(double startX, double startY, Enemy target, int damage, Type type, double speed, double splashRadius) {
        super(startX, startY, "Projectile");
        this.target = target;
        this.damage = damage;
        this.type = type;
        this.speed = speed;
        this.splashRadius = splashRadius;
        if (target != null) {
            this.angle = Math.atan2(target.getY() - startY, target.getX() - startX);
        }
    }

    public void update(List<Enemy> enemies) {
        if (!active) return;

        if (target == null || !target.isActive() || target.isDead()) {
            active = false;
            return;
        }

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dist = Math.hypot(dx, dy);
        this.angle = Math.atan2(dy, dx);

        if (dist <= speed) {
            hitTarget(enemies);
            active = false;
        } else {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }
    }

    private void hitTarget(List<Enemy> enemies) {
        if (splashRadius > 0) {
            for (Enemy enemy : enemies) {
                if (enemy.isActive() && !enemy.isDead() && distanceTo(enemy) <= splashRadius) {
                    enemy.takeDamage(damage);
                }
            }
        } else {
            if (target != null && target.isActive()) {
                target.takeDamage(damage);
            }
        }
    }

    @Override
    public void update() {
        // Handled by update(List<Enemy>)
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        switch (type) {
            case ARROW:
                BufferedImage arrowImg = ImageLoader.getImage("arrow.jpg", true);
                if (arrowImg == null) {
                    arrowImg = ImageLoader.getImage("arrow.png", true);
                }

                if (arrowImg != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.translate(x, y);
                    g2d.rotate(angle);
                    int w = 24;
                    int h = 24;
                    g2d.drawImage(arrowImg, -w / 2, -h / 2, w, h, null);
                    g2d.dispose();
                } else {
                    g.setColor(new Color(139, 69, 19));
                    g.fillOval((int) x - 3, (int) y - 3, 6, 6);
                }
                break;
            case MAGIC:
                BufferedImage magicImg = ImageLoader.getImage("firebal.jpg", true);
                if (magicImg == null) {
                    magicImg = ImageLoader.getImage("firebal.png", true);
                }

                if (magicImg != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.translate(x, y);
                    g2d.rotate(angle);
                    int size = 28;
                    g2d.drawImage(magicImg, -size / 2, -size / 2, size, size, null);
                    g2d.dispose();
                } else {
                    g.setColor(new Color(186, 85, 211));
                    g.fillOval((int) x - 6, (int) y - 6, 12, 12);
                    g.setColor(Color.WHITE);
                    g.fillOval((int) x - 3, (int) y - 3, 6, 6);
                }
                break;
            case HOLY:
                BufferedImage holyImg = ImageLoader.getImage("holybal.jpg", true);
                if (holyImg == null) {
                    holyImg = ImageLoader.getImage("holybal.png", true);
                }

                if (holyImg != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.translate(x, y);
                    g2d.rotate(angle);
                    int size = 26;
                    g2d.drawImage(holyImg, -size / 2, -size / 2, size, size, null);
                    g2d.dispose();
                } else {
                    g.setColor(Color.YELLOW);
                    g.fillOval((int) x - 5, (int) y - 5, 10, 10);
                }
                break;
        }
    }
}
