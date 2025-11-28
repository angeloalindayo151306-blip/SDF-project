package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import budgetsystem.util.LocalDatabase;

public class WelcomeFrame extends JFrame {
    public WelcomeFrame() {
        setTitle("Department Budget Management System - Welcome");
        setSize(700, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        getContentPane().setBackground(new Color(34, 34, 34));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("<html><center><span style='font-size:20px;color:#ffffff;'>Department Budget Management System</span></center></html>", SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(title, BorderLayout.NORTH);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        loginBtn.setPreferredSize(new Dimension(160, 50));
        registerBtn.setPreferredSize(new Dimension(160, 50));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.add(loginBtn);
        center.add(registerBtn);
        add(center, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setOpaque(false);
        JLabel info = new JLabel("Loaded: " + LocalDatabase.students.size() + " students, " +
                LocalDatabase.officers.size() + " officers, Dean: " + (LocalDatabase.dean != null));
        info.setForeground(Color.LIGHT_GRAY);
        south.add(info);
        add(south, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        registerBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
    }
}