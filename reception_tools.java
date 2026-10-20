package hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.net.URL;

public final class reception_tools {
    private reception_tools() { }

    private static final Color GOLD = new Color(212, 175, 55);
    private static final int POPUP_WIDTH = 1000;
    private static final int POPUP_HEIGHT = 700;

    public static void newCustomer() {
        JFrame frame = base("New Customer Check-in", 850, 520);
        JPanel background = new JPanel(null);
        background.setBackground(ui.NAVY);
        frame.setContentPane(background);

        JPanel overlay = new JPanel();
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, POPUP_WIDTH, POPUP_HEIGHT);
        background.add(overlay);

        JLabel title = new JLabel("NEW CUSTOMER CHECK-IN");
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 18, POPUP_WIDTH, 45);
        overlay.add(title);

        int labelX = 60, fieldX = 210, labelWidth = 140, fieldWidth = 200, fieldHeight = 32, startY = 80, gap = 45;

        JTextField name = createStyledField(overlay, "Guest Name", labelX, startY, labelWidth, fieldWidth, fieldHeight);
        JTextField phone = createStyledField(overlay, "Phone", labelX, startY + gap, labelWidth, fieldWidth, fieldHeight);
        JTextField email = createStyledField(overlay, "Email", labelX, startY + gap * 2, labelWidth, fieldWidth, fieldHeight);
        JTextField room = createStyledField(overlay, "Room Number", labelX, startY + gap * 3, labelWidth, fieldWidth, fieldHeight);
        JTextField checkOut = createStyledField(overlay, "Check-out Date", labelX, startY + gap * 4, labelWidth, fieldWidth, fieldHeight);
        checkOut.setToolTipText("Format: YYYY-MM-DD (optional)");
        for (JTextField f : new JTextField[]{name, phone, email, room, checkOut}) f.setBackground(ui.NAVY_DARK);
        JLabel dateHint = new JLabel("YYYY-MM-DD (optional)");
        dateHint.setFont(new Font("Tahoma", Font.PLAIN, 12));
        dateHint.setForeground(new Color(220, 220, 220));
        dateHint.setBounds(420, startY + gap * 4, 200, fieldHeight);
        overlay.add(dateHint);

        JButton save = createStyledButton("Confirm Check-in", new Color(37, 99, 235), 210, 350, 150, 42);
        JButton cancel = createStyledButton("Cancel", new Color(100, 116, 139), 370, 350, 150, 42);
        overlay.add(save); overlay.add(cancel);

        save.addActionListener(e -> {
            String roomNum = room.getText().trim();
            if (name.getText().trim().isEmpty() || roomNum.isEmpty()) { warn(frame, "Guest name and room number are required."); return; }
            LocalDate departure;
            try {
                departure = checkOut.getText().trim().isEmpty() ? null : LocalDate.parse(checkOut.getText().trim());
            } catch (DateTimeParseException ex) { warn(frame, "Check-out date must be in YYYY-MM-DD format, e.g. " + LocalDate.now().plusDays(2)); return; }
            if (departure != null && departure.isBefore(LocalDate.now())) { warn(frame, "Check-out date cannot be in the past."); return; }
            try (Connection c = hotel_db.connect()) {
                String availability = roomAvailability(c, roomNum);
                if (availability == null) { warn(frame, "Room " + roomNum + " does not exist. Ask the owner to add it first."); return; }
                if (!"Available".equalsIgnoreCase(availability)) { warn(frame, "Room " + roomNum + " is not available (" + availability + ")."); return; }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO customer (name, phone, email, room_number, check_in, check_out, status) VALUES (?, ?, ?, ?, ?, ?, 'Checked In')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name.getText().trim()); ps.setString(2, phone.getText().trim()); ps.setString(3, email.getText().trim()); ps.setString(4, roomNum); ps.setDate(5, Date.valueOf(LocalDate.now()));
                    if (departure == null) ps.setNull(6, Types.DATE); else ps.setDate(6, Date.valueOf(departure));
                    ps.executeUpdate();
                    String newId = "";
                    try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) newId = " Customer ID: " + keys.getInt(1); }
                    setRoomAvailability(c, roomNum, "Occupied");
                    JOptionPane.showMessageDialog(frame, "Guest checked in successfully." + newId); frame.dispose();
                }
            } catch (Exception ex) { error(frame, ex); }
        });
        cancel.addActionListener(e -> frame.dispose());
        frame.setVisible(true);
    }

    public static void updateCustomer() {
        Integer customerId = askCustomerId("Enter customer ID to update:", "Update Customer");
        if (customerId == null) return;
        try (Connection c = hotel_db.connect(); PreparedStatement find = c.prepareStatement("SELECT name, phone, email, room_number, status FROM customer WHERE id=?")) {
            find.setInt(1, customerId);
            ResultSet rs = find.executeQuery();
            if (!rs.next()) { JOptionPane.showMessageDialog(null, "Customer ID was not found."); return; }
            String oldRoom = rs.getString(4);
            boolean checkedIn = "Checked In".equalsIgnoreCase(rs.getString(5));

            JFrame frame = base("Update Customer Details", 850, 520);
            BackgroundPanel background = new BackgroundPanel("/icon/image.png");
            background.setLayout(null);
            frame.setContentPane(background);

            JPanel overlay = new JPanel();
            overlay.setLayout(null);
            overlay.setOpaque(false);
            overlay.setBounds(0, 0, POPUP_WIDTH, POPUP_HEIGHT);
            background.add(overlay);

            JLabel title = new JLabel("UPDATE CUSTOMER DETAILS");
            title.setFont(new Font("Tahoma", Font.BOLD, 28));
            title.setForeground(Color.WHITE);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            title.setBounds(0, 18, POPUP_WIDTH, 45);
            overlay.add(title);

            int labelX = 60, fieldX = 210, labelWidth = 140, fieldWidth = 200, fieldHeight = 32, startY = 80, gap = 45;

            JTextField name = createStyledField(overlay, "Guest Name", labelX, startY, labelWidth, fieldWidth, fieldHeight);
            name.setText(rs.getString(1));
            JTextField phone = createStyledField(overlay, "Phone Number", labelX, startY + gap, labelWidth, fieldWidth, fieldHeight);
            phone.setText(rs.getString(2));
            JTextField email = createStyledField(overlay, "Email Address", labelX, startY + gap * 2, labelWidth, fieldWidth, fieldHeight);
            email.setText(rs.getString(3));
            JTextField room = createStyledField(overlay, "Room Number", labelX, startY + gap * 3, labelWidth, fieldWidth, fieldHeight);
            room.setText(rs.getString(4));

            JButton save = createStyledButton("Update Details", new Color(37, 99, 235), 210, 350, 150, 42);
            JButton cancel = createStyledButton("Cancel", new Color(100, 116, 139), 370, 350, 150, 42);
            overlay.add(save); overlay.add(cancel);

            save.addActionListener(e -> {
                String newRoom = room.getText().trim();
                if (name.getText().trim().isEmpty() || newRoom.isEmpty()) { warn(frame, "Guest name and room number are required."); return; }
                // Open a fresh connection: the one used to load the form is already closed by now.
                try (Connection conn = hotel_db.connect()) {
                    boolean roomChanged = !newRoom.equals(oldRoom);
                    if (roomChanged) {
                        String availability = roomAvailability(conn, newRoom);
                        if (availability == null) { warn(frame, "Room " + newRoom + " does not exist."); return; }
                        if (checkedIn && !"Available".equalsIgnoreCase(availability)) { warn(frame, "Room " + newRoom + " is not available (" + availability + ")."); return; }
                    }
                    try (PreparedStatement update = conn.prepareStatement("UPDATE customer SET name=?, phone=?, email=?, room_number=? WHERE id=?")) {
                        update.setString(1, name.getText().trim()); update.setString(2, phone.getText().trim()); update.setString(3, email.getText().trim()); update.setString(4, newRoom); update.setInt(5, customerId); update.executeUpdate();
                    }
                    if (roomChanged && checkedIn) {
                        if (oldRoom != null) setRoomAvailability(conn, oldRoom, "Available");
                        setRoomAvailability(conn, newRoom, "Occupied");
                    }
                    JOptionPane.showMessageDialog(frame, "Customer details updated successfully.");
                    frame.dispose();
                } catch (Exception ex) { error(frame, ex); }
            });
            cancel.addActionListener(e -> frame.dispose());
            frame.setVisible(true);
        } catch (Exception e) { error(null, e); }
    }

    public static void roomService() {
        JFrame frame = base("Room Service Order", 850, 520);
        BackgroundPanel background = new BackgroundPanel("/icon/image.png");
        background.setLayout(null);
        frame.setContentPane(background);

        JPanel overlay = new JPanel();
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, POPUP_WIDTH, POPUP_HEIGHT);
        background.add(overlay);

        JLabel title = new JLabel("ORDER ROOM SERVICE");
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 18, POPUP_WIDTH, 45);
        overlay.add(title);

        int labelX = 60, fieldX = 210, labelWidth = 140, fieldWidth = 200, fieldHeight = 32, startY = 80, gap = 45;

        JTextField customerId = createStyledField(overlay, "Customer ID", labelX, startY, labelWidth, fieldWidth, fieldHeight);
        JTextField service = createStyledField(overlay, "Service Name", labelX, startY + gap, labelWidth, fieldWidth, fieldHeight);
        JTextField cost = createStyledField(overlay, "Cost", labelX, startY + gap * 2, labelWidth, fieldWidth, fieldHeight);

        JButton save = createStyledButton("Add Order", new Color(37, 99, 235), 210, 350, 150, 42);
        JButton cancel = createStyledButton("Cancel", new Color(100, 116, 139), 370, 350, 150, 42);
        overlay.add(save); overlay.add(cancel);

        save.addActionListener(e -> {
            if (customerId.getText().trim().isEmpty() || service.getText().trim().isEmpty() || cost.getText().trim().isEmpty()) { warn(frame, "All fields are required."); return; }
            int custId; double costValue;
            try { custId = Integer.parseInt(customerId.getText().trim()); } catch (NumberFormatException ex) { warn(frame, "Customer ID must be a number."); return; }
            try { costValue = Double.parseDouble(cost.getText().trim()); } catch (NumberFormatException ex) { warn(frame, "Cost must be a number."); return; }
            if (costValue <= 0) { warn(frame, "Cost must be greater than 0."); return; }
            try (Connection c = hotel_db.connect()) {
                try (PreparedStatement find = c.prepareStatement("SELECT status FROM customer WHERE id=?")) {
                    find.setInt(1, custId);
                    try (ResultSet rs = find.executeQuery()) {
                        if (!rs.next()) { warn(frame, "Customer ID was not found."); return; }
                        if (!"Checked In".equalsIgnoreCase(rs.getString(1))) { warn(frame, "This guest has already checked out."); return; }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO room_service (customer_id, service_name, cost, order_date) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, custId); ps.setString(2, service.getText().trim()); ps.setDouble(3, costValue); ps.setDate(4, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate(); JOptionPane.showMessageDialog(frame, "Service order added successfully."); frame.dispose();
                }
            } catch (Exception ex) { error(frame, ex); }
        });
        cancel.addActionListener(e -> frame.dispose());
        frame.setVisible(true);
    }

    public static void rooms() { tableWindow("Room Details", "SELECT room_number AS 'Room Number', availability AS 'Availability', price AS 'Price', cleaning_status AS 'Cleaning', bed_type AS 'Bed Type' FROM add_room ORDER BY room_number"); }
    public static void customerInfo() { tableWindow("Customer Information", "SELECT id AS 'ID', name AS 'Guest Name', phone AS 'Phone', email AS 'Email', room_number AS 'Room', check_in AS 'Check In', check_out AS 'Expected Check-out', status AS 'Status' FROM customer ORDER BY id DESC"); }
    public static void pickupService() { tableWindow("Pick-up Service - Available Drivers", "SELECT name AS 'Driver', car_model AS 'Car', location AS 'Location', availability AS 'Status' FROM driver ORDER BY availability, name"); }
    public static void departments() { tableWindow("Department / Employee List", "SELECT name AS 'Employee', job AS 'Department', phone AS 'Phone', email AS 'Email' FROM employee ORDER BY job, name"); }

    public static void checkOut() {
        Integer customerId = askCustomerId("Enter customer ID to check out:", "Check Out");
        if (customerId == null) return;
        try (Connection c = hotel_db.connect()) {
            String roomNum, status;
            try (PreparedStatement psFind = c.prepareStatement("SELECT room_number, status FROM customer WHERE id=?")) {
                psFind.setInt(1, customerId);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (!rs.next()) { JOptionPane.showMessageDialog(null, "Customer ID was not found."); return; }
                    roomNum = rs.getString(1); status = rs.getString(2);
                }
            }
            if ("Checked Out".equalsIgnoreCase(status)) { JOptionPane.showMessageDialog(null, "This guest has already checked out."); return; }
            try (PreparedStatement ps = c.prepareStatement("UPDATE customer SET status='Checked Out', check_out=? WHERE id=?")) {
                ps.setDate(1, Date.valueOf(LocalDate.now())); ps.setInt(2, customerId);
                ps.executeUpdate();
            }
            if (roomNum != null) setRoomAvailability(c, roomNum, "Available");
            JOptionPane.showMessageDialog(null, "Guest checked out successfully.");
        } catch (Exception e) { error(null, e); }
    }

    public static void generateBill() {
        Integer customerId = askCustomerId("Enter customer ID to generate bill:", "Billing");
        if (customerId == null) return;
        try (Connection c = hotel_db.connect()) {
            String guestName = "", roomNum = "";
            double roomPrice = 0;
            int days = 1;
            // Checked-out guests are billed up to their check-out date, current guests up to today. Minimum 1 night.
            try (PreparedStatement ps = c.prepareStatement("SELECT c.name, c.room_number, COALESCE(r.price, 0) AS price, "
                    + "GREATEST(DATEDIFF(IF(c.status='Checked Out' AND c.check_out IS NOT NULL, c.check_out, CURDATE()), c.check_in), 1) AS stay_days "
                    + "FROM customer c LEFT JOIN add_room r ON c.room_number=r.room_number WHERE c.id=?")) {
                ps.setInt(1, customerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    guestName = rs.getString("name"); roomNum = rs.getString("room_number"); roomPrice = rs.getDouble("price"); days = rs.getInt("stay_days");
                } else { JOptionPane.showMessageDialog(null, "Customer not found."); return; }
            }
            double serviceTotal = 0;
            try (PreparedStatement psS = c.prepareStatement("SELECT SUM(cost) FROM room_service WHERE customer_id=?")) {
                psS.setInt(1, customerId);
                ResultSet rsS = psS.executeQuery();
                if (rsS.next()) serviceTotal = rsS.getDouble(1);
            }
            double roomTotal = roomPrice * days;
            double finalTotal = roomTotal + serviceTotal;

            JFrame frame = base("Final Invoice", 400, 500);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(new Color(18, 34, 52));
            JLabel h1 = new JLabel("HOTEL MANAGEMENT SYSTEM");
            h1.setAlignmentX(Component.CENTER_ALIGNMENT);
            h1.setFont(new Font("Serif", Font.BOLD, 20));
            h1.setForeground(new Color(232, 201, 92));
            panel.add(h1);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            String billDetails = String.format("Guest: %s\nRoom: %s\nStay: %d days\n--------------------------\nRoom Cost: %.2f\nService Cost: %.2f\n--------------------------\nTOTAL AMOUNT: %.2f", guestName, roomNum, days, roomTotal, serviceTotal, finalTotal);
            JTextArea textArea = new JTextArea(billDetails);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            textArea.setForeground(Color.WHITE);
            textArea.setBackground(new Color(18, 34, 52));
            textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(textArea);
            JButton printBtn = button("Close", new Color(37, 99, 235));
            printBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            printBtn.addActionListener(e -> frame.dispose());
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(printBtn);
            frame.add(panel);
            frame.setVisible(true);
        } catch (Exception e) { error(null, e); }
    }

    private static void tableWindow(String title, String query) {
        JFrame frame = base(title, 920, 500); DefaultTableModel model = new DefaultTableModel(); JTable table = new JTable(model); table.setRowHeight(25);
        table.setBackground(new Color(18, 34, 52)); table.setForeground(Color.WHITE); table.setGridColor(new Color(212, 175, 55)); table.setSelectionBackground(new Color(37, 99, 235));
        table.getTableHeader().setBackground(new Color(10, 22, 36)); table.getTableHeader().setForeground(new Color(232, 201, 92));
        try (Connection c = hotel_db.connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData(); for (int i = 1; i <= meta.getColumnCount(); i++) model.addColumn(meta.getColumnLabel(i));
            while (rs.next()) { Object[] row = new Object[meta.getColumnCount()]; for (int i = 0; i < row.length; i++) row[i] = rs.getObject(i + 1); model.addRow(row); }
        } catch (Exception e) { error(frame, e); }
        JScrollPane scrollPane = new JScrollPane(table); scrollPane.getViewport().setBackground(new Color(18, 34, 52));
        JPanel content = new JPanel(new BorderLayout(0, 10)); content.setBackground(new Color(18, 34, 52)); content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); content.add(scrollPane, BorderLayout.CENTER);
        JButton back = button("Back", new Color(100, 116, 139)); back.addActionListener(e -> frame.dispose()); content.add(back, BorderLayout.SOUTH);
        frame.add(content); frame.setVisible(true);
    }

    private static JFrame base(String title, int width, int height) { JFrame f = new JFrame("Hotel Management System - " + title); f.setSize(POPUP_WIDTH, POPUP_HEIGHT); f.setLocationRelativeTo(null); f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); f.setResizable(false); f.getContentPane().setBackground(new Color(18, 34, 52)); return f; }

    private static JTextField createStyledField(JPanel p, String labelText, int lx, int ly, int lw, int fw, int fh) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setBounds(lx, ly, lw, fh);
        p.add(label);
        JTextField field = new JTextField();
        field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        field.setForeground(Color.WHITE);
        field.setBackground(Color.BLACK);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD, 2), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        field.setBounds(210, ly, fw, fh);
        p.add(field);
        return field;
    }

    private static JButton createStyledButton(String text, Color color, int x, int y, int w, int h) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        b.setBounds(x, y, w, h);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Asks for a customer ID. Returns null if cancelled or not a number. */
    private static Integer askCustomerId(String message, String title) {
        String id = JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
        if (id == null || id.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Customer ID must be a number.", "Hotel Management", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    /** Returns the room's availability, or null if the room does not exist. */
    static String roomAvailability(Connection c, String roomNum) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT availability FROM add_room WHERE room_number=?")) {
            ps.setString(1, roomNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String value = rs.getString(1);
                return value == null || value.trim().isEmpty() ? "Available" : value.trim();
            }
        }
    }

    static void setRoomAvailability(Connection c, String roomNum, String availability) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE add_room SET availability=? WHERE room_number=?")) {
            ps.setString(1, availability);
            ps.setString(2, roomNum);
            ps.executeUpdate();
        }
    }

    private static JButton button(String text, Color color) { JButton b = new JButton(text); b.setBackground(color); b.setForeground(Color.WHITE); b.setFocusPainted(false); return b; }
    private static void warn(Component parent, String text) { JOptionPane.showMessageDialog(parent, text, "Hotel Management", JOptionPane.WARNING_MESSAGE); }
    private static void error(Component parent, Exception e) { JOptionPane.showMessageDialog(parent, "Database error: " + e.getMessage(), "Hotel Management", JOptionPane.ERROR_MESSAGE); }

    /** Uses the shared image cache, so popups open instantly. */
    static class BackgroundPanel extends ui.BackgroundPanel {
        BackgroundPanel(String path) { super(path); }
    }
}
