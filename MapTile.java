package thearcher;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Represents a single grid tile on the map using images or fallback colors.
 */
public class MapTile {

    public enum Type {
        GRASS, // Green buildable area for towers
        PATH, // Enemy movement route
        CASTLE // Destination fortress
    }

    private int gridX;
    private int gridY;
    private int tileSize;
    private Type type;
    private boolean occupied;

    public MapTile(int gridX, int gridY, int tileSize, Type type) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.tileSize = tileSize;
        this.type = type;
        this.occupied = false;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public Type getType() {
        return type;
    }

    public boolean isBuildable() {
        return type == Type.GRASS && !occupied;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void render(Graphics2D g) {
        int px = gridX * tileSize;
        int py = gridY * tileSize;

        BufferedImage grassImg = ImageLoader.getImage("grass.png");

        switch (type) {
            case GRASS:
                if (grassImg != null) {
                    g.drawImage(grassImg, px, py, tileSize, tileSize, null);
                } else {
                    g.setColor(new Color(46, 139, 87)); // Sea Green
                    g.fillRect(px, py, tileSize, tileSize);
                    g.setColor(new Color(34, 110, 68));
                    g.drawRect(px, py, tileSize, tileSize); // Grid line
                }
                break;

            case PATH:
                // Draw grass under path first for transparent edges
                if (grassImg != null) {
                    g.drawImage(grassImg, px, py, tileSize, tileSize, null);
                }
                BufferedImage pathImg = ImageLoader.getImage("runway.png", true);
                if (pathImg != null) {
                    g.drawImage(pathImg, px, py, tileSize, tileSize, null);
                } else {
                    g.setColor(new Color(210, 180, 140)); // Tan / Dirt road
                    g.fillRect(px, py, tileSize, tileSize);
                    g.setColor(new Color(180, 150, 110));
                    g.drawRect(px, py, tileSize, tileSize);
                }
                break;

            case CASTLE:
                // Draw grass under castle first so white background becomes transparent grass
                if (grassImg != null) {
                    g.drawImage(grassImg, px, py, tileSize, tileSize, null);
                }
                BufferedImage castleImg = ImageLoader.getImage("castle.png", true);
                if (castleImg != null) {
                    // Enlarge castle to 80x80 px (1.6x larger) centered over the tile
                    int castleSize = 80;
                    int offset = (castleSize - tileSize) / 2;
                    g.drawImage(castleImg, px - offset, py - offset, castleSize, castleSize, null);
                } else {
                    g.setColor(new Color(112, 128, 144)); // Slate Gray Fortress
                    g.fillRect(px, py, tileSize, tileSize);
                    g.setColor(Color.YELLOW);
                    g.drawRect(px + 4, py + 4, tileSize - 8, tileSize - 8);
                }
                break;
        }
    }
}
