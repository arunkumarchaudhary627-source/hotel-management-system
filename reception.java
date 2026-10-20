package hotel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class reception extends JFrame {

    Color blue = new Color(37, 99, 235);
    Color green = new Color(22, 163, 74);
    Color purple = new Color(124, 58, 237);
    Color orange = new Color(234, 88, 12);
    Color red = new Color(220, 38, 38);
    Color teal = new Color(13, 148, 136);
    Color indigo = new Color(79, 70, 229);

    reception() {
        ui.mainFrame(this, "Hotel Management System - Reception");
        Dimension size = ui.mainSize();
        int W = size.width, H = size.height;

        ui.BackgroundPanel background = new ui.BackgroundPanel(ui.BACKGROUND);
        setContentPane(background);

        // =====================================================
        // LEFT SIDEBAR
        // =====================================================
        int sideW = 270, sideH = H - 70;
        ui.GlassPanel sidebar = new ui.GlassPanel(new Color(10, 22, 36, 225), 18, true);
        sidebar.setBounds(20, 20, sideW, sideH);
        background.add(sidebar);

        JLabel title = ui.title("RECEPTION", 22);
        title.setBounds(0, 20, sideW, 30);
        sidebar.add(title);

        int x = 30, bw = sideW - 60, bh = 38, step = 46;
        int y = 70;

        y = section(sidebar, "FRONT DESK", x, y, bw);
        y = add(sidebar, "New Customer Form", blue, x, y, bw, bh, step, reception_tools::newCustomer);
        y = add(sidebar, "Room", green, x, y, bw, bh, step, reception_tools::rooms);
        y = add(sidebar, "Department", purple, x, y, bw, bh, step, reception_tools::departments);
        y = add(sidebar, "Customer Info", orange, x, y, bw, bh, step, reception_tools::customerInfo);

        y = section(sidebar, "MANAGEMENT", x, y + 6, bw);
        y = add(sidebar, "Check Out", red, x, y, bw, bh, step, reception_tools::checkOut);
        y = add(sidebar, "Update Details", indigo, x, y, bw, bh, step, reception_tools::updateCustomer);
        y = add(sidebar, "Pick Up Service", teal, x, y, bw, bh, step, reception_tools::pickupService);
        y = add(sidebar, "Room Service", new Color(139, 69, 19), x, y, bw, bh, step, reception_tools::roomService);
        add(sidebar, "Billing", new Color(21, 128, 61), x, y, bw, bh, step, reception_tools::generateBill);

        JButton back = ui.button("Back to Dashboard", ui.MUTED);
        back.setBounds(x, sideH - 30 - bh, bw, bh);
        back.addActionListener(e -> ui.go(this, dashboard::new));
        sidebar.add(back);

        // =====================================================
        // WELCOME HEADER
        // =====================================================
        int hx = 20 + sideW + 20, hw = W - hx - 20 - 16;
        ui.GlassPanel header = new ui.GlassPanel();
        header.setBounds(hx, 20, hw, 96);
        background.add(header);

        JLabel welcome = ui.title("Welcome to the Front Desk", 24);
        welcome.setHorizontalAlignment(SwingConstants.LEFT);
        welcome.setBounds(28, 18, hw - 56, 32);
        header.add(welcome);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        JLabel info = new JLabel(today + "   •   Choose an option from the menu on the left");
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setForeground(new Color(220, 220, 220));
        info.setBounds(28, 54, hw - 56, 22);
        header.add(info);

        setVisible(true);
    }

    private static int section(JPanel p, String text, int x, int y, int w) {
        JLabel l = ui.text(text, 12, new Color(180, 186, 196));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(212, 175, 55, 110)));
        l.setBounds(x, y, w, 20);
        p.add(l);
        return y + 30;
    }

    private static int add(JPanel p, String text, Color color, int x, int y, int w, int h, int step, Runnable action) {
        JButton b = ui.button(text, color);
        b.setBounds(x, y, w, h);
        b.addActionListener(e -> action.run());
        p.add(b);
        return y + step;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(reception::new);
    }
}
