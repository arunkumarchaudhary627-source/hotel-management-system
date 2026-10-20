package hotel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class login extends JFrame implements ActionListener {

    JTextField textField1;
    JPasswordField passwordField1;
    JButton b1, b2;

    login() {
        super("Hotel Management System");
        int W = 760, H = 440;

        ui.BackgroundPanel background = new ui.BackgroundPanel(ui.BACKGROUND);
        setContentPane(background);

        // ---------------- LOGIN CARD ----------------
        ui.GlassPanel card = new ui.GlassPanel();
        card.setBounds((W - 345) / 2, (H - 28 - 300) / 2, 345, 300); // centred
        background.add(card);

        JLabel heading = ui.title("STAFF LOGIN", 22);
        heading.setBounds(0, 18, 345, 30);
        card.add(heading);

        JLabel label1 = ui.text("Username", 14, Color.WHITE);
        label1.setBounds(35, 65, 275, 22);
        card.add(label1);
        textField1 = (JTextField) ui.field(new JTextField());
        textField1.setBounds(35, 89, 275, 34);
        card.add(textField1);

        JLabel label2 = ui.text("Password", 14, Color.WHITE);
        label2.setBounds(35, 133, 275, 22);
        card.add(label2);
        passwordField1 = (JPasswordField) ui.field(new JPasswordField());
        passwordField1.setBounds(35, 157, 275, 34);
        card.add(passwordField1);

        b1 = ui.button("Login", new Color(37, 99, 235));
        b1.setBounds(35, 220, 130, 40);
        b1.addActionListener(this);
        card.add(b1);

        b2 = ui.button("Cancel", ui.MUTED);
        b2.setBounds(180, 220, 130, 40);
        b2.addActionListener(this);
        card.add(b2);

        getRootPane().setDefaultButton(b1); // press Enter to log in

        setSize(W, H);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        SwingUtilities.invokeLater(textField1::requestFocusInWindow);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b1) {
            String username = textField1.getText().trim();
            String password = String.valueOf(passwordField1.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password");
                return;
            }

            try {
                String q = "SELECT 1 FROM `user` WHERE username = ? AND password = ?";
                try (Connection connection = hotel_db.connect();
                     PreparedStatement preparedStatement = connection.prepareStatement(q)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            ui.go(this, dashboard::new); // straight to the dashboard, no extra popup
                        } else {
                            JOptionPane.showMessageDialog(this, "Wrong username or password");
                            passwordField1.setText("");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        } else if (e.getSource() == b2) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(login::new);
    }
}
