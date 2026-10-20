package hotel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class dashboard extends JFrame implements ActionListener {

    JButton add, rec;

    dashboard() {
        ui.mainFrame(this, "Hotel Management System");
        Dimension size = ui.mainSize();
        int W = size.width, H = size.height;

        ui.BackgroundPanel background = new ui.BackgroundPanel(ui.BACKGROUND);
        setContentPane(background);

        // ---------------- BACK TO LOGIN ----------------
        JButton back = ui.button("Back to Login", ui.MUTED);
        back.setBounds(25, 25, 150, 38);
        back.addActionListener(e -> ui.go(this, login::new));
        background.add(back);

        // ---------------- CENTRE CARD ----------------
        int cw = 640, ch = 390;
        ui.GlassPanel card = new ui.GlassPanel();
        card.setBounds((W - cw) / 2, (H - ch) / 2 - 20, cw, ch);
        background.add(card);

        JLabel title = ui.title("HOTEL MANAGEMENT SYSTEM", 28);
        title.setBounds(0, 28, cw, 36);
        card.add(title);

        JLabel subtitle = new JLabel("Choose your workspace", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(new Color(220, 220, 220));
        subtitle.setBounds(0, 68, cw, 22);
        card.add(subtitle);

        int img = 170, btnW = 200, gap = 80;
        int leftX = (cw - (btnW * 2 + gap)) / 2, rightX = leftX + btnW + gap;

        // ---------------- RECEPTION ----------------
        JLabel recLabel = new JLabel(ui.icon("icon/receptionist.png", img, img));
        recLabel.setBounds(leftX + (btnW - img) / 2, 115, img, img);
        card.add(recLabel);

        rec = ui.button("RECEPTION", new Color(37, 99, 235));
        rec.setFont(new Font("SansSerif", Font.BOLD, 18));
        rec.setBounds(leftX, 305, btnW, 52);
        rec.addActionListener(this);
        card.add(rec);

        // ---------------- OWNER ----------------
        JLabel bossLabel = new JLabel(ui.icon("icon/boss.png", img, img));
        bossLabel.setBounds(rightX + (btnW - img) / 2, 115, img, img);
        card.add(bossLabel);

        add = ui.button("OWNER", new Color(145, 105, 40));
        add.setFont(new Font("SansSerif", Font.BOLD, 18));
        add.setBounds(rightX, 305, btnW, 52);
        add.addActionListener(this);
        card.add(add);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == rec) {
            ui.go(this, reception::new);
        } else {
            ui.go(this, owner::new);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(dashboard::new);
    }
}
