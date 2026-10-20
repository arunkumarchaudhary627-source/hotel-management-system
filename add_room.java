package hotel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;

public class add_room extends JFrame implements ActionListener {

    JTextField t2, t4;
    JComboBox<String> t3, t5, t6;
    JButton b1, b2, b3, b4;

    private final Color GOLD = new Color(212, 175, 55);
    private final Color DARK_NAVY = new Color(18, 34, 52);

    public add_room() {
        setTitle("Hotel Management System - Add Room");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        // =====================================================
        // BACKGROUND
        // =====================================================
        BackgroundPanel background = new BackgroundPanel();
        background.setLayout(null);
        setContentPane(background);

        // =====================================================
        // OVERLAY
        // =====================================================
        JPanel overlay = new JPanel();
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, 1000, 700);
        background.add(overlay);

        // =====================================================
        // TITLE
        // =====================================================
        JLabel title = new JLabel("ADD ROOM DETAILS");
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 18, 1000, 45);
        overlay.add(title);

        int labelX = 60, fieldX = 210, labelWidth = 140, fieldWidth = 200, fieldHeight = 32, startY = 80, gap = 45;

        t2 = createStyledField(overlay, "Room Number", labelX, startY, labelWidth, fieldX, fieldWidth, fieldHeight);
        t3 = createStyledCombo(overlay, "Availability", labelX, startY + gap, labelWidth, fieldX, fieldWidth, fieldHeight, new String[]{"Available", "Occupied"});
        t4 = createStyledField(overlay, "Price", labelX, startY + gap * 2, labelWidth, fieldX, fieldWidth, fieldHeight);
        t5 = createStyledCombo(overlay, "Cleaning Status", labelX, startY + gap * 3, labelWidth, fieldX, fieldWidth, fieldHeight, new String[]{"Cleaned", "Dirty"});
        t6 = createStyledCombo(overlay, "Bed Type", labelX, startY + gap * 4, labelWidth, fieldX, fieldWidth, fieldHeight, new String[]{"Single Bed", "Double Bed"});

        b1 = createStyledButton("Add Room", new Color(37, 99, 235), 60, 345, 130, 42);
        b2 = createStyledButton("Update", new Color(22, 163, 74), 205, 345, 120, 42);
        b3 = createStyledButton("Clear", new Color(230, 145, 55), 340, 345, 110, 42);
        b4 = createStyledButton("Back", new Color(142, 98, 190), 465, 345, 110, 42);

        overlay.add(b1); overlay.add(b2); overlay.add(b3); overlay.add(b4);

        b1.addActionListener(this); b2.addActionListener(this); b3.addActionListener(this); b4.addActionListener(this);

        setVisible(true);
    }

    private JTextField createStyledField(JPanel p, String labelText, int lx, int ly, int lw, int fx, int fw, int fh) {
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
        field.setBounds(fx, ly, fw, fh);
        p.add(field);
        return field;
    }

    private JComboBox<String> createStyledCombo(JPanel p, String labelText, int lx, int ly, int lw, int fx, int fw, int fh, String[] items) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setBounds(lx, ly, lw, fh);
        p.add(label);
        JComboBox<String> combo = new JComboBox<>(items);
        ui.combo(combo);
        combo.setBounds(fx, ly, fw, fh);
        p.add(combo);
        return combo;
    }

    private JButton createStyledButton(String text, Color color, int x, int y, int w, int h) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        b.setBounds(x, y, w, h);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private Connection getConnection() throws SQLException {
        return hotel_db.connect();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b1) {
            String roomNumber = t2.getText().trim();
            String availability = (String) t3.getSelectedItem();
            String price = t4.getText().trim();
            String cleaning = (String) t5.getSelectedItem();
            String bedType = (String) t6.getSelectedItem();

            if (roomNumber.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Room Number and Price.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!roomNumber.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Room Number must contain numbers only.", "Invalid Room Number", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double priceValue = Double.parseDouble(price);
                if (priceValue <= 0) {
                    JOptionPane.showMessageDialog(this, "Price must be greater than 0.", "Invalid Price", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String sql = "INSERT INTO add_room (room_number, availability, price, cleaning_status, bed_type) VALUES (?, ?, ?, ?, ?)";
                try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, roomNumber); pst.setString(2, availability); pst.setDouble(3, priceValue); pst.setString(4, cleaning); pst.setString(5, bedType);
                    if (pst.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Room Added Successfully!");
                        clearFields();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == b2) {
            String roomNumber = t2.getText().trim();
            String availability = (String) t3.getSelectedItem();
            String price = t4.getText().trim();
            String cleaning = (String) t5.getSelectedItem();
            String bedType = (String) t6.getSelectedItem();
            if (roomNumber.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Room Number and Price.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double priceValue = Double.parseDouble(price);
                String sql = "UPDATE add_room SET availability=?, price=?, cleaning_status=?, bed_type=? WHERE room_number=?";
                try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, availability); pst.setDouble(2, priceValue); pst.setString(3, cleaning); pst.setString(4, bedType); pst.setString(5, roomNumber);
                    if (pst.executeUpdate() > 0) JOptionPane.showMessageDialog(this, "Room Updated Successfully!");
                    else JOptionPane.showMessageDialog(this, "Room not found.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == b3) {
            clearFields();
        } else if (e.getSource() == b4) {
            if (JOptionPane.showConfirmDialog(this, "Do you want to go back?", "Back", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose();
        }
    }

    private void clearFields() {
        t2.setText(""); t4.setText(""); t3.setSelectedIndex(0); t5.setSelectedIndex(0); t6.setSelectedIndex(0); t2.requestFocus();
    }

    class BackgroundPanel extends JPanel {
        BackgroundPanel() {
            setBackground(DARK_NAVY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new add_room());
    }
}
