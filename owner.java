package hotel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class owner extends JFrame implements ActionListener {

    JButton add_employe, add_room, add_Drivers, manage_rooms, manage_emp, report, back, logout;

    owner() {
        ui.mainFrame(this, "Hotel Management - Owner");
        Dimension size = ui.mainSize();
        int W = size.width, H = size.height;

        ui.BackgroundPanel background = new ui.BackgroundPanel(ui.BACKGROUND);
        setContentPane(background);

        // ---------------- CENTRE CARD ----------------
        int cw = 820, ch = 560;
        ui.GlassPanel card = new ui.GlassPanel();
        card.setBounds((W - cw) / 2, (H - ch) / 2 - 15, cw, ch);
        background.add(card);

        JLabel title = ui.title("OWNER PANEL", 28);
        title.setBounds(0, 25, cw, 36);
        card.add(title);

        // ---------------- LEFT COLUMN: ADD NEW ----------------
        int colW = 330, leftX = 60, rightX = cw - 60 - colW, top = 95;

        JLabel addHeader = section("ADD NEW");
        addHeader.setBounds(leftX, top, colW, 22);
        card.add(addHeader);

        add_employe = ui.button("Add Employee", new Color(52, 152, 219));
        add_room = ui.button("Add Room", new Color(46, 125, 92));
        add_Drivers = ui.button("Add Drivers", new Color(142, 108, 178));
        String[] icons = {"icon/employee.png", "icon/room.png", "icon/driver.png"};
        JButton[] adds = {add_employe, add_room, add_Drivers};
        for (int i = 0; i < adds.length; i++) {
            int y = top + 40 + i * 110;
            JLabel icon = new JLabel(ui.icon(icons[i], 80, 80));
            icon.setBounds(leftX, y, 80, 80);
            card.add(icon);
            adds[i].setBounds(leftX + 100, y + 18, colW - 100, 46);
            adds[i].addActionListener(this);
            card.add(adds[i]);
        }

        // ---------------- RIGHT COLUMN: MANAGE ----------------
        JLabel manageHeader = section("MANAGE & REPORTS");
        manageHeader.setBounds(rightX, top, colW, 22);
        card.add(manageHeader);

        manage_rooms = ui.button("Manage Rooms", new Color(37, 99, 235));
        manage_emp = ui.button("Manage Employees", new Color(22, 163, 74));
        report = ui.button("Revenue Report", new Color(230, 126, 34));
        JButton[] manages = {manage_rooms, manage_emp, report};
        for (int i = 0; i < manages.length; i++) {
            manages[i].setBounds(rightX, top + 58 + i * 110, colW, 46);
            manages[i].addActionListener(this);
            card.add(manages[i]);
        }

        // ---------------- BOTTOM BUTTONS ----------------
        back = ui.button("Back", ui.MUTED);
        back.setBounds(leftX, ch - 70, 140, 42);
        back.addActionListener(this);
        card.add(back);

        logout = ui.button("Logout", new Color(192, 75, 75));
        logout.setBounds(cw - 60 - 140, ch - 70, 140, 42);
        logout.addActionListener(this);
        card.add(logout);

        setVisible(true);
    }

    private static JLabel section(String text) {
        JLabel l = ui.text(text, 13, new Color(200, 200, 200));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(212, 175, 55, 120)));
        return l;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == add_employe) {
            new addemploye();
        } else if (e.getSource() == add_room) {
            new add_room();
        } else if (e.getSource() == add_Drivers) {
            new add_driver();
        } else if (e.getSource() == manage_rooms) {
            owner_tools.manageRooms();
        } else if (e.getSource() == manage_emp) {
            owner_tools.manageEmployees();
        } else if (e.getSource() == report) {
            owner_tools.revenueReport();
        } else if (e.getSource() == back) {
            ui.go(this, dashboard::new);
        } else if (e.getSource() == logout) {
            ui.go(this, login::new);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(owner::new);
    }
}
