package hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;

public class add_driver extends JFrame {
    private JTextField nameField, ageField, carField, locationField;
    private JComboBox<String> genderBox, availableBox;
    private DefaultTableModel model;
    private JTable table;

    private final Color GOLD = new Color(212, 175, 55);
    private final Color DARK_NAVY = new Color(18, 34, 52);

    public add_driver() {
        setTitle("Hotel Management System - Driver Details");
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
        // MAIN OVERLAY
        // =====================================================
        JPanel overlay = new JPanel();
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, 1000, 700);
        background.add(overlay);

        // =====================================================
        // TITLE
        // =====================================================
        JLabel title = new JLabel("ADD DRIVER DETAILS");
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 18, 1000, 45);
        overlay.add(title);

        // =====================================================
        // FORM PANEL (STYLED)
        // =====================================================
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(new Color(10, 22, 36));
        formPanel.setBounds(50, 80, 900, 220);
        overlay.add(formPanel);

        int labelX = 40, fieldX = 180, labelWidth = 130, fieldWidth = 200, fieldHeight = 32, startY = 30, gap = 40;

        nameField = createStyledField(formPanel, "Driver Name", labelX, startY, labelWidth, fieldX, fieldWidth, fieldHeight);
        ageField = createStyledField(formPanel, "Age", labelX, startY + gap, labelWidth, fieldX, fieldWidth, fieldHeight);

        // Gender Combo
        JLabel gLabel = new JLabel("Gender");
        gLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        gLabel.setForeground(Color.WHITE);
        gLabel.setBounds(labelX, startY + gap * 2, labelWidth, fieldHeight);
        formPanel.add(gLabel);
        genderBox = createStyledCombo(new String[]{"Male", "Female", "Other"}, fieldX, startY + gap * 2, fieldWidth, fieldHeight);
        formPanel.add(genderBox);

        carField = createStyledField(formPanel, "Car Model", labelX, startY + gap * 3, labelWidth, fieldX, fieldWidth, fieldHeight);

        // Availability Combo
        JLabel aLabel = new JLabel("Availability");
        aLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        aLabel.setForeground(Color.WHITE);
        aLabel.setBounds(labelX, startY + gap * 4, labelWidth, fieldHeight); // This will overflow the panel, let's adjust layout
        // Let's use a 2-column grid for labels/fields to be safer
        // I'll move the combos and remaining fields to a second column

        // Moving to a 2-column approach within the formPanel
        int col2LabelX = 420, col2FieldX = 560;

        // Availability
        JLabel avLabel = new JLabel("Availability");
        avLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        avLabel.setForeground(Color.WHITE);
        avLabel.setBounds(col2LabelX, startY, labelWidth, fieldHeight);
        formPanel.add(avLabel);
        availableBox = createStyledCombo(new String[]{"Available", "On Trip", "Off Duty"}, col2FieldX, startY, fieldWidth, fieldHeight);
        formPanel.add(availableBox);

        // Location
        locationField = createStyledField(formPanel, "Current Location", col2LabelX, startY + gap, labelWidth, col2FieldX, fieldWidth, fieldHeight);

        // =====================================================
        // FORM BUTTONS
        // =====================================================
        JButton save = createStyledButton("Save Driver", new Color(37, 99, 235), 420, 160, 140, 35);
        JButton update = createStyledButton("Update Selected", new Color(22, 163, 74), 570, 160, 140, 35);
        JButton delete = createStyledButton("Delete Selected", new Color(220, 38, 38), 720, 160, 140, 35);
        JButton clear = createStyledButton("Clear", new Color(100, 116, 139), 870, 160, 100, 35);
        JButton back = createStyledButton("Back", new Color(142, 98, 190), 50, 612, 110, 32);
        // Keep all four buttons inside the 900px-wide form panel
        // (right of the Car Model field, which ends at x=380)
        save.setBounds(400, 160, 118, 35);
        update.setBounds(524, 160, 118, 35);
        delete.setBounds(648, 160, 118, 35);
        clear.setBounds(772, 160, 110, 35);

        formPanel.add(save); formPanel.add(update); formPanel.add(delete); formPanel.add(clear);
        overlay.add(back);

        // =====================================================
        // TABLE PANEL
        // =====================================================
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBounds(50, 320, 900, 280);
        tablePanel.setOpaque(false); // Make panel transparent
        overlay.add(tablePanel);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Gender", "Car", "Availability", "Location"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(10, 22, 36)); // Solid dark (translucent colours cause repaint glitches in Swing)
        table.setGridColor(GOLD);
        table.setOpaque(true);
        table.setSelectionBackground(new Color(37, 99, 235));
        table.setSelectionForeground(Color.WHITE);

        // Style Table Header
        table.getTableHeader().setBackground(new Color(30, 30, 30));
        table.getTableHeader().setForeground(GOLD);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(10, 22, 36));
        scrollPane.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // =====================================================
        // ACTIONS
        // =====================================================
        save.addActionListener(e -> saveDriver());
        update.addActionListener(e -> updateDriver());
        delete.addActionListener(e -> deleteDriver());
        clear.addActionListener(e -> clearForm());
        back.addActionListener(e -> dispose());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) loadSelection(table.getSelectedRow());
        });

        loadDrivers();
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

    private JComboBox<String> createStyledCombo(String[] items, int x, int y, int w, int h) {
        JComboBox<String> combo = new JComboBox<>(items);
        ui.combo(combo);
        combo.setBounds(x, y, w, h);
        return combo;
    }

    private JButton createStyledButton(String text, Color color, int x, int y, int w, int h) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        b.setBounds(x, y, w, h);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private boolean valid() {
        try {
            if (nameField.getText().trim().isEmpty() || carField.getText().trim().isEmpty() || Integer.parseInt(ageField.getText().trim()) < 18)
                throw new IllegalArgumentException();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Name, age (18+) and car model are required.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private void saveDriver() {
        if (!valid()) return;
        String sql = "INSERT INTO driver (name, age, gender, car_model, availability, location) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = hotel_db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Driver added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadDrivers();
        } catch (SQLException e) { databaseError(e); }
    }

    private void updateDriver() {
        int id = selectedId();
        if (id < 0) { JOptionPane.showMessageDialog(this, "Select a driver first.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        if (!valid()) return;
        String sql = "UPDATE driver SET name=?, age=?, gender=?, car_model=?, availability=?, location=? WHERE id=?";
        try (Connection con = hotel_db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps); ps.setInt(7, id); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Driver details updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadDrivers();
        } catch (SQLException e) { databaseError(e); }
    }

    private void deleteDriver() {
        int id = selectedId();
        if (id < 0) { JOptionPane.showMessageDialog(this, "Select a driver first.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Remove this driver?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = hotel_db.connect(); PreparedStatement ps = con.prepareStatement("DELETE FROM driver WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
            clearForm(); loadDrivers();
        } catch (SQLException e) { databaseError(e); }
    }

    private void fill(PreparedStatement ps) throws SQLException {
        ps.setString(1, nameField.getText().trim());
        ps.setInt(2, Integer.parseInt(ageField.getText().trim()));
        ps.setString(3, (String) genderBox.getSelectedItem());
        ps.setString(4, carField.getText().trim());
        ps.setString(5, (String) availableBox.getSelectedItem());
        ps.setString(6, locationField.getText().trim());
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
    }

    private void loadSelection(int row) {
        nameField.setText(String.valueOf(model.getValueAt(row, 1)));
        ageField.setText(String.valueOf(model.getValueAt(row, 2)));
        genderBox.setSelectedItem(model.getValueAt(row, 3));
        carField.setText(String.valueOf(model.getValueAt(row, 4)));
        availableBox.setSelectedItem(model.getValueAt(row, 5));
        locationField.setText(String.valueOf(model.getValueAt(row, 6)));
    }

    private void loadDrivers() {
        model.setRowCount(0);
        try (Connection con = hotel_db.connect(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT id, name, age, gender, car_model, availability, location FROM driver ORDER BY name")) {
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)});
        } catch (SQLException e) { databaseError(e); }
    }

    private void clearForm() {
        table.clearSelection();
        nameField.setText("");
        ageField.setText("");
        carField.setText("");
        locationField.setText("");
        genderBox.setSelectedIndex(0);
        availableBox.setSelectedIndex(0);
    }

    private void databaseError(Exception e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    class BackgroundPanel extends JPanel {
        BackgroundPanel() {
            setBackground(DARK_NAVY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(add_driver::new);
    }
}
