package hotel;

import javax.swing.*;

public class splash extends JFrame {

    private static final int MIN_SHOW_MS = 1500;

    splash() {
        int w = 858, h = 572; // same 3:2 shape as splash.png, so it is not cropped
        JLabel label = new JLabel(ui.icon("icon/splash.png", w, h));
        label.setBounds(0, 0, w, h);
        setLayout(null);
        add(label);
        setUndecorated(true);
        setSize(w, h);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        long start = System.currentTimeMillis();
        // Load images + connect to MySQL while the splash is showing.
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                ui.preload();
                long left = MIN_SHOW_MS - (System.currentTimeMillis() - start);
                if (left > 0) {
                    try { Thread.sleep(left); } catch (InterruptedException ignored) { }
                }
                return null;
            }
            @Override protected void done() {
                ui.go(splash.this, login::new);
            }
        }.execute();
    }

    /** Application entry point. Run this class from IntelliJ IDEA. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing's default look and feel is a safe fallback.
            }
            new splash();
        });
    }
}
