package hotel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Shared UI helpers.
 * - Images are loaded from disk ONCE and every resized copy is cached, so opening a page is instant.
 * - All main pages use the same size, fitted to the screen.
 */
public final class ui {
    private ui() { }

    // ---------------- theme ----------------
    public static final Color NAVY = new Color(18, 34, 52);
    public static final Color NAVY_DARK = new Color(10, 22, 36);
    public static final Color GOLD = new Color(212, 175, 55);
    public static final Color GOLD_LIGHT = new Color(232, 201, 92);
    public static final Color GLASS = new Color(10, 22, 36, 200);
    public static final Color MUTED = new Color(100, 116, 139);

    public static final String BACKGROUND = "icon/image.png";

    // ---------------- page size ----------------
    private static Dimension mainSize;

    /** Size used by Dashboard, Owner and Reception: 1280x800, shrunk to fit smaller screens. */
    public static synchronized Dimension mainSize() {
        if (mainSize == null) {
            Rectangle screen;
            try {
                screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            } catch (Exception e) {
                screen = new Rectangle(0, 0, 1280, 800);
            }
            int w = Math.max(1024, Math.min(1280, screen.width - 40));
            int h = Math.max(680, Math.min(800, screen.height - 40));
            mainSize = new Dimension(w, h);
        }
        return mainSize;
    }

    /** Applies the standard main-page size and centres the window. */
    public static void mainFrame(JFrame f, String title) {
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setSize(mainSize());
        f.setLocationRelativeTo(null);
    }

    /** Opens the next page first and only then closes the current one, so there is no blank gap. */
    public static void go(Window current, Supplier<? extends Window> next) {
        Window w = next.get();
        if (w != null) w.toFront();
        if (current != null) current.dispose();
    }

    // ---------------- image cache ----------------
    private static final Map<String, BufferedImage> originals = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> scaled = new ConcurrentHashMap<>();

    /** Loads an image from the classpath once. Returns null if it is missing. */
    public static BufferedImage image(String path) {
        String key = path.startsWith("/") ? path.substring(1) : path;
        BufferedImage cached = originals.get(key);
        if (cached != null) return cached;
        try {
            URL url = ui.class.getClassLoader().getResource(key);
            if (url == null) return null;
            BufferedImage img = ImageIO.read(url);
            if (img != null) originals.put(key, img);
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    /** Returns a resized copy of an image, computed once per size and then reused. */
    public static BufferedImage scaled(String path, int w, int h) {
        if (w <= 0 || h <= 0) return null;
        String key = path + "@" + w + "x" + h;
        BufferedImage cached = scaled.get(key);
        if (cached != null) return cached;
        BufferedImage src = image(path);
        if (src == null) return null;
        BufferedImage out = resize(src, w, h);
        scaled.put(key, out);
        return out;
    }

    /** Image scaled to cover w x h (cropped in the centre, no stretching). */
    public static BufferedImage cover(String path, int w, int h) {
        if (w <= 0 || h <= 0) return null;
        String key = path + "#cover" + w + "x" + h;
        BufferedImage cached = scaled.get(key);
        if (cached != null) return cached;
        BufferedImage src = image(path);
        if (src == null) return null;
        double s = Math.max((double) w / src.getWidth(), (double) h / src.getHeight());
        int sw = (int) Math.ceil(src.getWidth() * s), sh = (int) Math.ceil(src.getHeight() * s);
        BufferedImage big = resize(src, sw, sh);
        BufferedImage out = big.getSubimage((sw - w) / 2, (sh - h) / 2, w, h);
        scaled.put(key, out);
        return out;
    }

    public static ImageIcon icon(String path, int w, int h) {
        BufferedImage img = scaled(path, w, h);
        return img == null ? new ImageIcon() : new ImageIcon(img);
    }

    /** Fast, good-quality resize (halves step by step for big reductions). */
    private static BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage current = src;
        int cw = src.getWidth(), ch = src.getHeight();
        int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        while (cw / 2 >= w && ch / 2 >= h) {
            cw /= 2; ch /= 2;
            current = draw(current, cw, ch, type);
        }
        return draw(current, w, h, type);
    }

    private static BufferedImage draw(BufferedImage src, int w, int h, int type) {
        BufferedImage out = new BufferedImage(w, h, type);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    /** Called from the splash screen: loads images and the database in the background. */
    public static void preload() {
        String[] all = {BACKGROUND, "icon/boss.png", "icon/receptionist.png",
                "icon/employee.png", "icon/room.png", "icon/driver.png"};
        for (String p : all) image(p);
        Dimension m = mainSize();
        for (double s : new double[]{1, 2}) {
            cover(BACKGROUND, (int) (m.width * s), (int) (m.height * s));
            cover(BACKGROUND, (int) (760 * s), (int) (440 * s));
        }
        try (java.sql.Connection ignored = hotel_db.connect()) {
            // Warms up the MySQL driver and creates tables, so the first login is instant.
        } catch (Exception ignored) {
            // The login screen will show the real error if MySQL is not running.
        }
    }

    // ---------------- components ----------------

    /** Full-page background photo. Resized once per window size, then simply blitted. */
    public static class BackgroundPanel extends JPanel {
        private final String path;
        public BackgroundPanel(String path) {
            this.path = path.startsWith("/") ? path.substring(1) : path;
            setLayout(null);
            setBackground(NAVY);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform t = g2.getTransform();
            double sx = Math.max(1, t.getScaleX()), sy = Math.max(1, t.getScaleY()); // Retina = 2
            BufferedImage img = cover(path, (int) Math.round(getWidth() * sx), (int) Math.round(getHeight() * sy));
            if (img != null) g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        }
    }

    /** Dark, semi-transparent rounded card drawn correctly (Swing can't paint alpha backgrounds itself). */
    public static class GlassPanel extends JPanel {
        private final Color fill;
        private final int radius;
        private final boolean border;
        public GlassPanel(Color fill, int radius, boolean border) {
            this.fill = fill; this.radius = radius; this.border = border;
            setOpaque(false);
            setLayout(null);
        }
        public GlassPanel() { this(GLASS, 18, true); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D r = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, radius, radius);
            g2.setColor(fill);
            g2.fill(r);
            if (border) {
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 170));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(r);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JLabel title(String text, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, size));
        l.setForeground(GOLD_LIGHT);
        return l;
    }

    public static JLabel text(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    /** Flat coloured button with a hover effect. */
    public static JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color hover = color.brighter();
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(color); }
        });
        return b;
    }

    public static JTextField field(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setForeground(Color.WHITE);
        f.setBackground(Color.BLACK);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD, 2), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return f;
    }

    /**
     * Dark dropdown that looks the same on every OS. The macOS look-and-feel ignores setBackground
     * and paints a white box, which made the white selected text invisible.
     */
    public static <T> JComboBox<T> combo(JComboBox<T> combo) {
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrow = new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.SOUTH, Color.BLACK, Color.BLACK, GOLD, Color.BLACK);
                arrow.setBorder(BorderFactory.createEmptyBorder());
                return arrow;
            }
        });
        combo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        combo.setForeground(Color.WHITE);
        combo.setBackground(Color.BLACK);
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createLineBorder(GOLD, 2));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                setBackground(selected && index >= 0 ? GOLD : Color.BLACK);
                setForeground(selected && index >= 0 ? Color.BLACK : Color.WHITE);
                return this;
            }
        });
        return combo;
    }
}
