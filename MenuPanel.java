package thearcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Main menu panel displayed before the game starts.
 * Shows game title, character previews, and Play/Exit buttons.
 */
public class MenuPanel extends JPanel {

    private final MainFrame mainFrame;

    // Animation state
    private float glowAlpha = 0f;
    private boolean glowUp = true;
    private Timer animTimer;

    // Particle system for background
    private float[] particleX   = new float[60];
    private float[] particleY   = new float[60];
    private float[] particleSpd = new float[60];
    private float[] particleAlp = new float[60];

    // Character info for preview cards
    private static final String[] CHAR_NAMES  = {"Archer", "Mage", "Priest"};
    private static final String[] CHAR_DESCS  = {"Single Target\nLong Range", "AoE Splash\nDamage", "Buff Ally\nDamage"};
    private static final String[] CHAR_COSTS  = {"100G", "150G", "120G"};
    private static final Color[]  CHAR_COLORS = {
        new Color(80, 200, 90),
        new Color(100, 140, 240),
        new Color(240, 200, 60)
    };
    private static final String[] CHAR_IMG_PATHS = {
        "images/archer.png",
        "images/mage.png",
        "images/priest.png"
    };

    // Hover state for cards
    private int hoveredCard = -1;

    // Button references
    private Rectangle playBtnBounds = new Rectangle();
    private Rectangle exitBtnBounds = new Rectangle();
    private boolean hoverPlay = false;
    private boolean hoverExit = false;

    public MenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(980, 500));
        setBackground(new Color(10, 10, 20));
        initParticles();
        setupListeners();

        // Animation timer ~60 fps
        animTimer = new Timer(16, e -> {
            // Glow pulse
            if (glowUp) {
                glowAlpha += 0.02f;
                if (glowAlpha >= 1f) { glowAlpha = 1f; glowUp = false; }
            } else {
                glowAlpha -= 0.02f;
                if (glowAlpha <= 0f) { glowAlpha = 0f; glowUp = true; }
            }
            // Move particles upward
            for (int i = 0; i < particleX.length; i++) {
                particleY[i] -= particleSpd[i];
                if (particleY[i] < 0) {
                    particleY[i] = 500 + (float)(Math.random() * 100);
                    particleX[i] = (float)(Math.random() * 980);
                }
            }
            repaint();
        });
        animTimer.start();
    }

    private void initParticles() {
        for (int i = 0; i < particleX.length; i++) {
            particleX[i]   = (float)(Math.random() * 980);
            particleY[i]   = (float)(Math.random() * 500);
            particleSpd[i] = 0.3f + (float)(Math.random() * 0.7f);
            particleAlp[i] = 0.2f + (float)(Math.random() * 0.5f);
        }
    }

    private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (playBtnBounds.contains(e.getPoint())) {
                    animTimer.stop();
                    mainFrame.startGame();
                } else if (exitBtnBounds.contains(e.getPoint())) {
                    System.exit(0);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hoverPlay = false;
                hoverExit = false;
                hoveredCard = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean changed = false;
                boolean hp = playBtnBounds.contains(e.getPoint());
                boolean he = exitBtnBounds.contains(e.getPoint());
                if (hp != hoverPlay || he != hoverExit) { hoverPlay = hp; hoverExit = he; changed = true; }

                // Check card hover
                int cx = getWidth() / 2;
                int cardY = 260;
                int cardW = 140, cardH = 150;
                int gap   = 20;
                int totalW = 3 * cardW + 2 * gap;
                int startX = cx - totalW / 2;
                int newHov = -1;
                for (int i = 0; i < 3; i++) {
                    int rx = startX + i * (cardW + gap);
                    if (new Rectangle(rx, cardY, cardW, cardH).contains(e.getPoint())) {
                        newHov = i;
                        break;
                    }
                }
                if (newHov != hoveredCard) { hoveredCard = newHov; changed = true; }
                if (changed) repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // --- Background gradient ---
        GradientPaint bgGrad = new GradientPaint(0, 0, new Color(8, 8, 22), 0, H, new Color(20, 10, 40));
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, W, H);

        // --- Particles ---
        for (int i = 0; i < particleX.length; i++) {
            g2.setColor(new Color(1f, 1f, 1f, particleAlp[i] * 0.6f));
            int sz = 2 + (int)(particleAlp[i] * 2);
            g2.fillOval((int)particleX[i], (int)particleY[i], sz, sz);
        }

        // --- Glowing background circle behind title ---
        float glow = 0.08f + glowAlpha * 0.12f;
        RadialGradientPaint rgp = new RadialGradientPaint(
            new Point2D.Float(W / 2f, 110),
            260,
            new float[]{0f, 1f},
            new Color[]{new Color(120, 60, 255, (int)(glow * 255)), new Color(0, 0, 0, 0)}
        );
        g2.setPaint(rgp);
        g2.fillOval(W / 2 - 260, 110 - 260, 520, 520);

        // --- TITLE ---
        g2.setFont(new Font("SansSerif", Font.BOLD, 52));
        String title = "THE ARCHER";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (W - fm.stringWidth(title)) / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(title, tx + 3, 83);

        // Gradient text effect
        GradientPaint titleGrad = new GradientPaint(
            tx, 40, new Color(255, 200, 60),
            tx + fm.stringWidth(title), 80, new Color(255, 100, 30)
        );
        g2.setPaint(titleGrad);
        g2.drawString(title, tx, 80);

        // --- Subtitle ---
        g2.setFont(new Font("SansSerif", Font.ITALIC, 16));
        String sub = "Tower Defense — Defend your Castle!";
        FontMetrics sfm = g2.getFontMetrics();
        g2.setColor(new Color(200, 180, 255, 210));
        g2.drawString(sub, (W - sfm.stringWidth(sub)) / 2, 108);

        // --- Divider line ---
        g2.setColor(new Color(255, 200, 60, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(W / 2 - 260, 122, W / 2 + 260, 122);

        // --- Character Cards ---
        int cardW = 140, cardH = 150, gap = 20;
        int totalW = 3 * cardW + 2 * gap;
        int startX = W / 2 - totalW / 2;
        int cardY = 165;

        for (int i = 0; i < 3; i++) {
            int rx = startX + i * (cardW + gap);
            boolean hov = (hoveredCard == i);

            // Card raise on hover
            int cardDrawY = hov ? cardY - 6 : cardY;

            // Card background
            if (hov) {
                g2.setColor(new Color(CHAR_COLORS[i].getRed(), CHAR_COLORS[i].getGreen(), CHAR_COLORS[i].getBlue(), 40));
                g2.fillRoundRect(rx - 4, cardDrawY - 4, cardW + 8, cardH + 8, 18, 18);
            }
            GradientPaint cardGrad = new GradientPaint(
                rx, cardDrawY, new Color(35, 35, 60),
                rx, cardDrawY + cardH, new Color(20, 20, 40)
            );
            g2.setPaint(cardGrad);
            g2.fillRoundRect(rx, cardDrawY, cardW, cardH, 14, 14);

            // Card border
            g2.setStroke(new BasicStroke(hov ? 2f : 1f));
            g2.setColor(hov ? CHAR_COLORS[i] : new Color(CHAR_COLORS[i].getRed(), CHAR_COLORS[i].getGreen(), CHAR_COLORS[i].getBlue(), 100));
            g2.drawRoundRect(rx, cardDrawY, cardW, cardH, 14, 14);

            // Character image
            BufferedImage img = ImageLoader.getImage(CHAR_IMG_PATHS[i], true);
            int imgSize = 52;
            int imgX = rx + cardW / 2 - imgSize / 2;
            int imgY = cardDrawY + 12;
            if (img != null) {
                g2.drawImage(img, imgX, imgY, imgSize, imgSize, null);
            } else {
                g2.setColor(CHAR_COLORS[i]);
                g2.fillOval(imgX + 8, imgY + 8, imgSize - 16, imgSize - 16);
            }

            // Name
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics nfm = g2.getFontMetrics();
            g2.setColor(CHAR_COLORS[i]);
            g2.drawString(CHAR_NAMES[i], rx + (cardW - nfm.stringWidth(CHAR_NAMES[i])) / 2, cardDrawY + 78);

            // Description (multi-line)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(200, 200, 200));
            String[] lines = CHAR_DESCS[i].split("\n");
            for (int li = 0; li < lines.length; li++) {
                FontMetrics dfm = g2.getFontMetrics();
                g2.drawString(lines[li], rx + (cardW - dfm.stringWidth(lines[li])) / 2, cardDrawY + 95 + li * 14);
            }

            // Cost badge
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(rx + cardW / 2 - 22, cardDrawY + cardH - 26, 44, 18, 8, 8);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics cfm = g2.getFontMetrics();
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(CHAR_COSTS[i], rx + (cardW - cfm.stringWidth(CHAR_COSTS[i])) / 2, cardDrawY + cardH - 12);
        }

        // --- Buttons ---
        int btnW = 180, btnH = 48;
        int btnY = cardY + cardH + 20;
        int playX = W / 2 - btnW - 20;
        int exitX = W / 2 + 20;

        playBtnBounds.setBounds(playX, btnY, btnW, btnH);
        exitBtnBounds.setBounds(exitX, btnY, btnW, btnH);

        drawButton(g2, playX, btnY, btnW, btnH, "PLAY GAME",
            hoverPlay ? new Color(100, 230, 100) : new Color(46, 180, 60),
            hoverPlay ? new Color(60, 180, 60) : new Color(30, 120, 40));

        drawButton(g2, exitX, btnY, btnW, btnH, "EXIT",
            hoverExit ? new Color(240, 100, 80) : new Color(190, 60, 50),
            hoverExit ? new Color(180, 60, 50) : new Color(130, 40, 35));

        // --- Version label ---
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(100, 100, 130));
        g2.drawString("v1.0  |  OOP Mini Project", 14, H - 10);
    }

    private void drawButton(Graphics2D g2, int x, int y, int w, int h,
                            String text, Color topColor, Color botColor) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(x + 3, y + 4, w, h, 12, 12);

        // Button gradient body
        GradientPaint btnGrad = new GradientPaint(x, y, topColor, x, y + h, botColor);
        g2.setPaint(btnGrad);
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // Border
        g2.setColor(topColor.brighter());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // Text
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Stop animation when switching away from menu */
    public void stopAnimation() {
        if (animTimer != null) animTimer.stop();
    }
}
