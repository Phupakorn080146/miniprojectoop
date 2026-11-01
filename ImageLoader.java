package thearcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Utility class to load, cache, and process PNG/JPG character image files.
 */
public class ImageLoader {
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    private static final String BASE_DIR = resolveBaseDir();

    private static String resolveBaseDir() {
        String[] candidates = {
                "thearcher/images/",
                "images/",
                "mini_project/thearcher/images/",
                "../thearcher/images/",
        };
        for (String path : candidates) {
            if (new File(path).isDirectory()) {
                return path;
            }
        }
        return "thearcher/images/";
    }

    /**
     * Load image and remove background using flood fill from corners.
     * 
     * @param filename e.g. "images/archer.png"
     * @param removeBg true = remove background if not already transparent
     */
    public static BufferedImage getImage(String filename, boolean removeBg) {
        String name = new File(filename).getName();
        String cacheKey = (removeBg ? "nobg_" : "") + name;

        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        String fullPath = BASE_DIR + name;
        try {
            File file = new File(fullPath);
            if (!file.exists()) {
                System.err.println("[ImageLoader] File not found: " + file.getAbsolutePath());
                return null;
            }
            BufferedImage loaded = ImageIO.read(file);

            // Convert to ARGB to support transparency
            BufferedImage img = new BufferedImage(loaded.getWidth(), loaded.getHeight(), BufferedImage.TYPE_INT_ARGB);
            img.getGraphics().drawImage(loaded, 0, 0, null);

            // Check if already has transparent pixels
            boolean alreadyHasAlpha = hasTransparentPixels(img);

            if (removeBg && !alreadyHasAlpha) {
                removeBackground(img, 35); // Lowered tolerance to 35 to preserve character details
            }

            cache.put(cacheKey, img);
            return img;
        } catch (Exception e) {
            System.err.println("[ImageLoader] Error loading: " + fullPath + " - " + e.getMessage());
        }
        return null;
    }

    private static boolean hasTransparentPixels(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        // Check corner pixels first
        int[] checkX = { 0, w - 1, 0, w - 1, w / 2 };
        int[] checkY = { 0, 0, h - 1, h - 1, 0 };
        for (int i = 0; i < checkX.length; i++) {
            int alpha = (img.getRGB(checkX[i], checkY[i]) >> 24) & 0xFF;
            if (alpha < 128)
                return true;
        }
        return false;
    }

    /** Convenience overload without background removal (original behavior). */
    public static BufferedImage getImage(String filename) {
        return getImage(filename, false);
    }

    /**
     * Cache for animation frame arrays loaded from folders.
     */
    private static final Map<String, BufferedImage[]> frameCache = new HashMap<>();

    /**
     * Load animation frames from a folder of individual PNG files.
     * Files are expected to be named: {prefix}1.png, {prefix}2.png, ... {prefix}N.png
     * e.g. folder="boss_walk", prefix="boss_walk_", frameCount=7
     *       => images/boss_walk/boss_walk_1.png .. images/boss_walk/boss_walk_7.png
     *
     * @param folder     subfolder name inside the images directory (e.g. "boss_walk")
     * @param prefix     filename prefix before the frame number  (e.g. "boss_walk_")
     * @param frameCount total number of frames
     * @param removeBg   true = remove solid/checker background
     * @return array of trimmed BufferedImage frames, or null on failure
     */
    public static BufferedImage[] getFramesFromFolder(String folder, String prefix, int frameCount, boolean removeBg) {
        String cacheKey = (removeBg ? "nobg_" : "") + folder + "/" + prefix + "_f" + frameCount;
        if (frameCache.containsKey(cacheKey)) {
            return frameCache.get(cacheKey);
        }

        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 1; i <= frameCount; i++) {
            String filename = prefix + i + ".png";
            String fullPath = BASE_DIR + folder + "/" + filename;
            try {
                File file = new File(fullPath);
                if (!file.exists()) {
                    System.err.println("[ImageLoader] Frame not found: " + file.getAbsolutePath());
                    return null;
                }
                BufferedImage loaded = ImageIO.read(file);

                // Convert to ARGB
                BufferedImage img = new BufferedImage(loaded.getWidth(), loaded.getHeight(), BufferedImage.TYPE_INT_ARGB);
                img.getGraphics().drawImage(loaded, 0, 0, null);

                boolean hasAlpha = hasTransparentPixels(img);
                if (removeBg && !hasAlpha) {
                    removeBackground(img, 35);
                }

                frames[i - 1] = trimFrame(img);
            } catch (Exception e) {
                System.err.println("[ImageLoader] Error loading frame: " + fullPath + " - " + e.getMessage());
                return null;
            }
        }

        frameCache.put(cacheKey, frames);
        return frames;
    }

    /**
     * Crop transparent borders from a BufferedImage.
     * Scans each edge inward until a non-transparent pixel is found, then returns
     * a tight sub-image containing only the visible character pixels.
     */
    private static BufferedImage trimFrame(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w, minY = h, maxX = 0, maxY = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 10) {  // visible pixel
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // If no visible pixels found, return original
        if (minX > maxX || minY > maxY) return img;

        int cropW = maxX - minX + 1;
        int cropH = maxY - minY + 1;
        BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
        cropped.getGraphics().drawImage(img, -minX, -minY, null);
        return cropped;
    }

    /**
     * Flood fill from all 4 corners to remove solid/checker background.
     * Any pixel connected to corners within 'tolerance' of local background color
     * becomes transparent.
     */
    private static void removeBackground(BufferedImage img, int tolerance) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean[][] visited = new boolean[w][h];

        Queue<int[]> queue = new LinkedList<>();

        // Seed flood fill from all 4 corners and borders
        int[][] seeds = {
                { 0, 0 }, { w - 1, 0 }, { 0, h - 1 }, { w - 1, h - 1 },
                { w / 2, 0 }, { 0, h / 2 }, { w - 1, h / 2 }, { w / 2, h - 1 }
        };

        for (int[] seed : seeds) {
            int sx = seed[0], sy = seed[1];
            if (!visited[sx][sy]) {
                int seedColor = img.getRGB(sx, sy);
                int r = (seedColor >> 16) & 0xFF;
                int g = (seedColor >> 8) & 0xFF;
                int b = seedColor & 0xFF;
                // Only seed if background looks like white, gray, or checkerboard (R/G/B
                // similar and high brightness)
                if (isBackgroundLikeColor(r, g, b)) {
                    queue.add(new int[] { sx, sy, r, g, b });
                    visited[sx][sy] = true;
                }
            }
        }

        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int cx = pos[0], cy = pos[1];
            int bgR = pos[2], bgG = pos[3], bgB = pos[4];

            // Make this pixel transparent
            img.setRGB(cx, cy, 0x00000000);

            for (int d = 0; d < 4; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visited[nx][ny]) {
                    int neighborRgb = img.getRGB(nx, ny);
                    if (isSimilarColor(neighborRgb, bgR, bgG, bgB, tolerance)) {
                        visited[nx][ny] = true;
                        queue.add(new int[] { nx, ny, bgR, bgG, bgB });
                    }
                }
            }
        }
    }

    private static boolean isBackgroundLikeColor(int r, int g, int b) {
        // White or light gray background
        int brightness = (r + g + b) / 3;
        int colorDiff = Math.max(Math.abs(r - g), Math.max(Math.abs(r - b), Math.abs(g - b)));
        return brightness > 180 && colorDiff < 30;
    }

    private static boolean isSimilarColor(int rgb, int targetR, int targetG, int targetB, int tolerance) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return Math.abs(r - targetR) <= tolerance &&
                Math.abs(g - targetG) <= tolerance &&
                Math.abs(b - targetB) <= tolerance;
    }
}
