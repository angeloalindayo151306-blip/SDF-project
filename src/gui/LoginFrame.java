package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import budgetsystem.util.LocalDatabase;
import budgetsystem.model.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginFrame() {
        setTitle("Login");
        setSize(520, 260);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(new Color(18,18,18));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);

        JLabel head = new JLabel("Login", SwingConstants.CENTER);
        head.setForeground(Color.WHITE);
        head.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        add(head, gbc);
        gbc.gridwidth=1;

        gbc.gridy++;
        add(new JLabel("Role:"), gbc);
        roleBox = new JComboBox<>(new String[] {"Student","Officer","Dean"}); gbc.gridx=1;
        add(roleBox, gbc);
        gbc.gridx=0; gbc.gridy++;

        add(new JLabel("Username:"), gbc); gbc.gridx=1;
        usernameField = new JTextField(18); add(usernameField, gbc);
        gbc.gridx=0; gbc.gridy++;

        add(new JLabel("Password:"), gbc); gbc.gridx=1;
        passwordField = new JPasswordField(18); add(passwordField, gbc);
        gbc.gridx=0; gbc.gridy++;

        JButton loginBtn = new JButton("Login");
        JButton back = new JButton("Back");
        gbc.gridx=0; add(loginBtn, gbc); gbc.gridx=1; add(back, gbc);

        loginBtn.addActionListener(e -> doLogin());
        back.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });
    }

    private void doLogin() {
        String role = (String) roleBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if ("Dean".equals(role)) {
            Dean d = LocalDatabase.dean;
            if (d != null && d.getUsername().equals(username) && d.getPassword().equals(password)) {
                new DeanFrame(d).setVisible(true); dispose(); return;
            } else {
                JOptionPane.showMessageDialog(this, "Invalid dean credentials.");
                return;
            }
        }

        if ("Officer".equals(role)) {
            for (Officer o : LocalDatabase.officers) {
                if (o.getUsername().equals(username) && o.getPassword().equals(password)) {
                    new OfficerFrame(o).setVisible(true); dispose(); return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid officer credentials.");
            return;
        }

        if ("Student".equals(role)) {
            for (Student s : LocalDatabase.students) {
                if (s.getUsername().equals(username) && s.getPassword().equals(password)) {
                    new StudentFrame(s).setVisible(true); dispose(); return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid student credentials.");
            return;
        }
    }
}
