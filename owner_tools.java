package hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public final class owner_tools {
    private static final int POPUP_WIDTH = 1000;
    private static final int POPUP_HEIGHT = 700;
    private owner_tools() { }

    public static void manageRooms() {
        JFrame frame = base("Manage Rooms", 920, 600);
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setRowHeight(25);
        styleTable(table);

        try (Connection c = hotel_db.connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM add_room ORDER BY room_number")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) model.addColumn(meta.getColumnLabel(i));
            while (rs.next()) {
                Object[] row = new Object[meta.getColumnCount()];
                for (int i = 0; i < row.length; i++) row[i] = rs.getObject(i + 1);
                model.addRow(row);
            }
        } catch (Exception e) { error(frame, e); }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(18, 34, 52));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(18, 34, 52));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton updateBtn = button("Update Selected Room", new Color(37, 99, 235));
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { warn(frame, "Please select a room first."); return; }
            String roomNum = String.valueOf(model.getValueAt(row, 0));
            updateRoomDetails(frame, roomNum);
        });
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setBackground(new Color(18, 34, 52));
        actions.add(updateBtn);
        JButton back = button("Back", new Color(100, 116, 139));
        back.addActionListener(e -> frame.dispose());
        actions.add(back);
        panel.add(actions, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void updateRoomDetails(JFrame frame, String roomNum) {
        JComboBox<String> availability = ui.combo(new JComboBox<>(new String[]{"Available", "Occupied"}));
        JTextField price = new JTextField();
        JComboBox<String> cleaning = ui.combo(new JComboBox<>(new String[]{"Cleaned", "Dirty"}));
        JComboBox<String> bed = ui.combo(new JComboBox<>(new String[]{"Single Bed", "Double Bed"}));
        styleField(price);

        // Pre-fill the form with the room's current values.
        try (Connection c = hotel_db.connect(); PreparedStatement ps = c.prepareStatement("SELECT availability, price, cleaning_status, bed_type FROM add_room WHERE room_number=?")) {
            ps.setString(1, roomNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { warn(frame, "Room " + roomNum + " no longer exists."); return; }
                selectOrAdd(availability, rs.getString(1));
                price.setText(String.valueOf(rs.getDouble(2)));
                selectOrAdd(cleaning, rs.getString(3));
                selectOrAdd(bed, rs.getString(4));
            }
        } catch (Exception ex) { error(frame, ex); return; }

        JFrame dialog = base("Update Room " + roomNum, 450, 330);
        dialog.setSize(450, 330);
        dialog.setLocationRelativeTo(frame);
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.setBackground(new Color(18, 34, 52));
        p.add(label("Availability:")); p.add(availability);
        p.add(label("Price:")); p.add(price);
        p.add(label("Cleaning Status:")); p.add(cleaning);
        p.add(label("Bed Type:")); p.add(bed);

        JButton save = button("Save Changes", new Color(37, 99, 235));
        save.addActionListener(e -> {
            double priceValue;
            try { priceValue = Double.parseDouble(price.getText().trim()); } catch (NumberFormatException ex) { warn(dialog, "Price must be a number."); return; }
            if (priceValue <= 0) { warn(dialog, "Price must be greater than 0."); return; }
            try (Connection c = hotel_db.connect(); PreparedStatement ps = c.prepareStatement("UPDATE add_room SET availability=?, price=?, cleaning_status=?, bed_type=? WHERE room_number=?")) {
                ps.setString(1, (String) availability.getSelectedItem());
                ps.setDouble(2, priceValue);
                ps.setString(3, (String) cleaning.getSelectedItem());
                ps.setString(4, (String) bed.getSelectedItem());
                ps.setString(5, roomNum);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Room updated successfully.");
                dialog.dispose();
                frame.dispose();
                manageRooms(); // Refresh the table
            } catch (Exception ex) { error(dialog, ex); }
        });
        JButton cancel = button("Cancel", new Color(100, 116, 139));
        cancel.addActionListener(e -> dialog.dispose());
        p.add(cancel); p.add(save);
        dialog.add(p);
        dialog.setVisible(true);
    }

    private static void selectOrAdd(JComboBox<String> combo, String value) {
        if (value == null || value.trim().isEmpty()) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(value.trim())) { combo.setSelectedIndex(i); return; }
        }
        combo.addItem(value.trim());
        combo.setSelectedItem(value.trim());
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        return l;
    }

    public static void manageEmployees() {
        JFrame frame = base("Manage Employees", 920, 600);
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setRowHeight(25);
        styleTable(table);

        try (Connection c = hotel_db.connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM employee ORDER BY name")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) model.addColumn(meta.getColumnLabel(i));
            while (rs.next()) {
                Object[] row = new Object[meta.getColumnCount()];
                for (int i = 0; i < row.length; i++) row[i] = rs.getObject(i + 1);
                model.addRow(row);
            }
        } catch (Exception e) { error(frame, e); }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(18, 34, 52));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(18, 34, 52));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton deleteBtn = button("Delete Employee", new Color(192, 75, 75));
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { warn(frame, "Please select an employee first."); return; }
            int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Key column is "id" or "employee_id" depending on how the table was created.
                String idColumn = model.getColumnName(0);
                try (Connection c = hotel_db.connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM employee WHERE `" + idColumn + "`=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Employee deleted successfully.");
                    manageEmployees(); // Refresh
                    frame.dispose();
                } catch (Exception ex) { error(frame, ex); }
            }
        });
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setBackground(new Color(18, 34, 52));
        actions.add(deleteBtn);
        JButton back = button("Back", new Color(100, 116, 139));
        back.addActionListener(e -> frame.dispose());
        actions.add(back);
        panel.add(actions, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    public static void revenueReport() {
        JFrame frame = base("Hotel Revenue Report", 500, 400);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(18, 34, 52));

        try (Connection c = hotel_db.connect()) {
            // Total Rooms
            int totalRooms = 0;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM add_room")) {
                if (rs.next()) totalRooms = rs.getInt(1);
            }

            // Occupied Rooms
            int occupiedRooms = 0;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM customer WHERE status='Checked In'")) {
                if (rs.next()) occupiedRooms = rs.getInt(1);
            }

            // Estimated Total Revenue (Room Price * Stay Days + Room Service)
            double totalRev = 0;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT SUM(r.price * GREATEST(DATEDIFF(IF(c.status='Checked Out' AND c.check_out IS NOT NULL, c.check_out, CURDATE()), c.check_in), 1)) FROM customer c JOIN add_room r ON c.room_number=r.room_number")) {
                if (rs.next()) totalRev = rs.getDouble(1);
            }

            double serviceRev = 0;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT SUM(cost) FROM room_service")) {
                if (rs.next()) serviceRev = rs.getDouble(1);
            }

            JLabel h1 = new JLabel("FINANCIAL OVERVIEW");
            h1.setAlignmentX(Component.CENTER_ALIGNMENT);
            h1.setFont(new Font("SansSerif", Font.BOLD, 22));
            h1.setForeground(new Color(232, 201, 92));
            panel.add(h1);
            panel.add(Box.createRigidArea(new Dimension(0, 30)));

            String report = String.format(
                "Total Rooms: %d\nOccupied Rooms: %d\nOccupancy Rate: %.2f%%\n--------------------------\nEstimated Room Revenue: %.2f\nTotal Service Revenue: %.2f\n--------------------------\nGROSS REVENUE: %.2f",
                totalRooms, occupiedRooms, totalRooms == 0 ? 0 : ((double) occupiedRooms / totalRooms) * 100, totalRev, serviceRev, totalRev + serviceRev
            );

            JTextArea area = new JTextArea(report);
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 16));
            area.setForeground(Color.WHITE);
            area.setBackground(new Color(18, 34, 52));
            area.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(area);

            JButton closeBtn = button("Close Report", new Color(37, 99, 235));
            closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            closeBtn.addActionListener(e -> frame.dispose());
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(closeBtn);

            frame.add(panel);
        } catch (Exception e) { error(frame, e); }
        frame.setVisible(true);
    }

    private static JFrame base(String title, int width, int height) {
        JFrame f = new JFrame("Hotel Management System - " + title);
        f.setSize(POPUP_WIDTH, POPUP_HEIGHT);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setResizable(false);
        f.getContentPane().setBackground(new Color(18, 34, 52));
        return f;
    }

    private static void styleTable(JTable table) {
        table.setBackground(new Color(18, 34, 52));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(212, 175, 55));
        table.setSelectionBackground(new Color(37, 99, 235));
        table.getTableHeader().setBackground(new Color(10, 22, 36));
        table.getTableHeader().setForeground(new Color(232, 201, 92));
    }

    private static void styleField(JTextField field) {
        field.setBackground(new Color(10, 22, 36));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(new Color(212, 175, 55), 1));
    }

    private static JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void warn(Component parent, String text) { JOptionPane.showMessageDialog(parent, text, "Hotel Management", JOptionPane.WARNING_MESSAGE); }
    private static void error(Component parent, Exception e) { JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Hotel Management", JOptionPane.ERROR_MESSAGE); }
}
