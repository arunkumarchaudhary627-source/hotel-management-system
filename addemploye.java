package hotel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import java.util.regex.Pattern;

public class addemploye extends JFrame implements ActionListener {

    private JTextField nameField, ageField, salaryField, phoneField, aadhaarField, emailField;
    private JRadioButton maleButton, femaleButton;
    private ButtonGroup genderGroup;
    private JComboBox<String> jobBox;
    private JButton saveButton, backButton;

    private final Color GOLD = new Color(212, 175, 55);
    private final Color BLUE = new Color(52, 152, 219);
    private final Color PURPLE = new Color(142, 98, 190);
    private final Color DARK_NAVY = new Color(18, 34, 52);

    public addemploye() {
        setTitle("Hotel Management System - Add Employee");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        BackgroundPanel background = new BackgroundPanel();
        background.setLayout(null);
        setContentPane(background);

        JPanel overlay = new JPanel();
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, 1000, 700);
        background.add(overlay);

        JLabel title = new JLabel("ADD EMPLOYEE DETAILS");
        title.setFont(new Font("Tahoma", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 18, 1000, 45);
        overlay.add(title);

        int labelX = 60, fieldX = 210, labelWidth = 140, fieldWidth = 200, fieldHeight = 32, startY = 80, gap = 45;

        nameField = createStyledField(overlay, "Name", labelX, startY, labelWidth, fieldX, fieldWidth, fieldHeight);
        ageField = createStyledField(overlay, "Age", labelX, startY + gap, labelWidth, fieldX, fieldWidth, fieldHeight);

        JLabel genderLabel = new JLabel("Gender");
        genderLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        genderLabel.setForeground(Color.WHITE);
        genderLabel.setBounds(labelX, startY + gap * 2, labelWidth, fieldHeight);
        overlay.add(genderLabel);

        maleButton = new JRadioButton("Male");
        maleButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        maleButton.setForeground(Color.WHITE);
        maleButton.setOpaque(false);
        maleButton.setBounds(fieldX, startY + gap * 2, 80, fieldHeight);
        overlay.add(maleButton);

        femaleButton = new JRadioButton("Female");
        femaleButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        femaleButton.setForeground(Color.WHITE);
        femaleButton.setOpaque(false);
        femaleButton.setBounds(fieldX + 85, startY + gap * 2, 90, fieldHeight);
        overlay.add(femaleButton);

        genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        maleButton.setSelected(true);

        JLabel jobLabel = new JLabel("Job");
        jobLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        jobLabel.setForeground(Color.WHITE);
        jobLabel.setBounds(labelX, startY + gap * 3, labelWidth, fieldHeight);
        overlay.add(jobLabel);
        jobBox = createStyledCombo(new String[]{"Front Desk Clerk", "Manager", "Accountant", "Housekeeping", "Chef", "Waiter", "Security", "Receptionist"}, fieldX, startY + gap * 3, fieldWidth, fieldHeight);
        overlay.add(jobBox);

        salaryField = createStyledField(overlay, "Salary", labelX, startY + gap * 4, labelWidth, fieldX, fieldWidth, fieldHeight);
        phoneField = createStyledField(overlay, "Phone", labelX, startY + gap * 5, labelWidth, fieldX, fieldWidth, fieldHeight);
        aadhaarField = createStyledField(overlay, "Aadhaar", labelX, startY + gap * 6, labelWidth, fieldX, fieldWidth, fieldHeight);
        emailField = createStyledField(overlay, "Email", labelX, startY + gap * 7, labelWidth, fieldX, fieldWidth, fieldHeight);

        // Below the last field (Email ends at y=427) so the buttons no longer cover Aadhaar/Email.
        saveButton = createStyledButton("SAVE", BLUE, 210, 460, 150, 42);
        backButton = createStyledButton("BACK", PURPLE, 370, 460, 150, 42);
        overlay.add(saveButton);
        overlay.add(backButton);

        saveButton.addActionListener(this);
        backButton.addActionListener(this);

        JLabel employeeImage = createEmployeeImage();
        employeeImage.setBounds(510, 125, 280, 260);
        overlay.add(employeeImage);

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
        b.setFont(new Font("Tahoma", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        b.setBounds(x, y, w, h);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel createEmployeeImage() {
        JLabel label = new JLabel(ui.icon("icon/employee.png", 240, 240));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private Connection getConnection() throws SQLException {
        return hotel_db.connect();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            saveEmployee();
        } else if (e.getSource() == backButton) {
            dispose();
        }
    }

    private void saveEmployee() {
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String salaryText = salaryField.getText().trim();
        String phone = phoneField.getText().trim();
        String aadhaar = aadhaarField.getText().trim();
        String email = emailField.getText().trim();
        String gender = maleButton.isSelected() ? "Male" : "Female";
        String job = jobBox.getSelectedItem().toString();

        if (name.isEmpty() || ageText.isEmpty() || salaryText.isEmpty() || phone.isEmpty() || aadhaar.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all employee details.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(this, "Name must contain letters only.", "Invalid Name", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int age = Integer.parseInt(ageText);
            if (age < 18 || age > 70) {
                JOptionPane.showMessageDialog(this, "Age must be between 18 and 70.", "Invalid Age", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double salary = Double.parseDouble(salaryText);
            if (salary <= 0) {
                JOptionPane.showMessageDialog(this, "Salary must be greater than 0.", "Invalid Salary", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "Phone number must contain exactly 10 digits.", "Invalid Phone", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!aadhaar.matches("\\d{12}")) {
                JOptionPane.showMessageDialog(this, "Aadhaar must contain exactly 12 digits.", "Invalid Aadhaar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email.", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sql = "INSERT INTO employee (name, age, gender, job, salary, phone, aadhaar, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name); statement.setInt(2, age); statement.setString(3, gender); statement.setString(4, job); statement.setDouble(5, salary); statement.setString(6, phone); statement.setString(7, aadhaar); statement.setString(8, email);
                if (statement.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Employee Added Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText(""); ageField.setText(""); salaryField.setText(""); phoneField.setText(""); aadhaarField.setText(""); emailField.setText("");
        maleButton.setSelected(true); jobBox.setSelectedIndex(0); nameField.requestFocus();
    }

    class BackgroundPanel extends JPanel {
        BackgroundPanel() {
            setBackground(DARK_NAVY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new addemploye());
    }
}
