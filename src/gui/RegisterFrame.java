package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;

public class RegisterFrame extends JFrame {

    private JComboBox<String> roleBox;
    private JPanel formPanel;

    private JTextField idField;
    private JTextField fnameField, mnameField, lnameField;
    private JTextField dobField, ageField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JComboBox<String> courseBox, yearBox, positionBox;

    private final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public RegisterFrame() {

        setTitle("Register");
        setSize(700, 520);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(new Color(24, 24, 24));
        setLayout(new BorderLayout());

        JLabel head = new JLabel("Register New Account", SwingConstants.CENTER);
        head.setForeground(Color.WHITE);
        head.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        head.setFont(new Font("Arial", Font.BOLD, 18));
        add(head, BorderLayout.NORTH);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.add(new JLabel("Select Role: "));
        roleBox = new JComboBox<>(new String[]{"Student", "Officer", "Dean"});
        top.add(roleBox);
        add(top, BorderLayout.PAGE_START);

        formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        add(formPanel, BorderLayout.CENTER);

        buildForm("Student");

        roleBox.addActionListener(e -> buildForm((String) roleBox.getSelectedItem()));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back");
        buttons.add(registerBtn);
        buttons.add(backBtn);
        add(buttons, BorderLayout.SOUTH);

        registerBtn.addActionListener(e -> onRegister());
        backBtn.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });

        if (LocalDatabase.dean != null) {
            ((DefaultComboBoxModel<String>) roleBox.getModel()).removeElement("Dean");
        }
    }

    private void buildForm(String role) {

        formPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;

        // FIRST NAME
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        fnameField = new JTextField(20);
        formPanel.add(fnameField, gbc);

        // MIDDLE NAME
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Middle Name:"), gbc);
        gbc.gridx = 1;
        mnameField = new JTextField(20);
        formPanel.add(mnameField, gbc);

        // LAST NAME
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        lnameField = new JTextField(20);
        formPanel.add(lnameField, gbc);

        // DOB - MM/dd/yyyy
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("DOB (MM/dd/yyyy):"), gbc);
        gbc.gridx = 1;
        dobField = new JTextField(12);
        formPanel.add(dobField, gbc);

        // AGE (auto)
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(6);
        ageField.setEditable(false);
        formPanel.add(ageField, gbc);

        dobField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateAge(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateAge(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateAge(); }
        });

        gbc.gridy++;

        // =======================================
        // STUDENT FIELDS
        // =======================================
        if (role.equals("Student")) {

            gbc.gridx = 0;
            formPanel.add(new JLabel("ID Number:"), gbc);
            gbc.gridx = 1;
            idField = new JTextField(12);
            formPanel.add(idField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            formPanel.add(new JLabel("Course:"), gbc);
            gbc.gridx = 1;
            courseBox = new JComboBox<>(new String[]{"BSIT","BSBA","BSED","BSE","BSC","CASS"});
            formPanel.add(courseBox, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            formPanel.add(new JLabel("Year Level:"), gbc);
            gbc.gridx = 1;
            yearBox = new JComboBox<>(new String[]{"1st Year","2nd Year","3rd Year","4th Year"});
            formPanel.add(yearBox, gbc);
        }

        // =======================================
        // OFFICER FIELDS
        // =======================================
        if (role.equals("Officer")) {

            gbc.gridx = 0;
            formPanel.add(new JLabel("ID Number:"), gbc);
            gbc.gridx = 1;
            idField = new JTextField(12);
            formPanel.add(idField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            formPanel.add(new JLabel("Course:"), gbc);
            gbc.gridx = 1;
            courseBox = new JComboBox<>(new String[]{"","BSIT","BSBA","BSED","BSE","BSC","CASS"});
            formPanel.add(courseBox, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            formPanel.add(new JLabel("Year Level:"), gbc);
            gbc.gridx = 1;
            yearBox = new JComboBox<>(new String[]{"","1st Year","2nd Year","3rd Year","4th Year"});
            formPanel.add(yearBox, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            formPanel.add(new JLabel("Position:"), gbc);
            gbc.gridx = 1;
            positionBox = new JComboBox<>(new String[]{
                    "President","VP Internal","VP External","Treasurer","Secretary","PIO"
            });
            formPanel.add(positionBox, gbc);
        }

        // Username
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(18);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(18);
        formPanel.add(passwordField, gbc);

        formPanel.revalidate();
        formPanel.repaint();
    }

    private void updateAge() {
        try {
            LocalDate dob = LocalDate.parse(dobField.getText().trim(), DOB_FORMAT);
            int age = Period.between(dob, LocalDate.now()).getYears();
            ageField.setText(String.valueOf(age));
        } catch (Exception e) {
            ageField.setText("");
        }
    }

    private void onRegister() {

        String role = (String) roleBox.getSelectedItem();
        String first = fnameField.getText().trim();
        String last  = lnameField.getText().trim();
        String user  = usernameField.getText().trim();
        String pass  = new String(passwordField.getPassword()).trim();

        if (first.isEmpty() || last.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobField.getText().trim(), DOB_FORMAT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DOB must be in MM/dd/yyyy format.");
            return;
        }

        int age = Period.between(dob, LocalDate.now()).getYears();

        if (role.equals("Dean")) {
            Dean d = new Dean(first, mnameField.getText().trim(), last,
                    dob.toString(), age, user, pass);

            if (!LocalDatabase.addDean(d)) {
                JOptionPane.showMessageDialog(this, "A Dean is already registered.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Dean Registered!");
            new WelcomeFrame().setVisible(true);
            dispose();
            return;
        }

        String id = idField.getText().trim();

        if (role.equals("Student")) {
            Student s = new Student(
                    id, first, mnameField.getText().trim(), last,
                    dob.toString(), age,
                    user, pass,
                    (String) courseBox.getSelectedItem(),
                    (String) yearBox.getSelectedItem()
            );
            LocalDatabase.addStudent(s);
            JOptionPane.showMessageDialog(this, "Student registered!");
        }

        if (role.equals("Officer")) {
            Officer o = new Officer(
                    id, first, mnameField.getText().trim(), last,
                    dob.toString(), age,
                    user, pass,
                    (String) courseBox.getSelectedItem(),
                    (String) yearBox.getSelectedItem(),
                    (String) positionBox.getSelectedItem()
            );
            LocalDatabase.addOfficer(o);
            JOptionPane.showMessageDialog(this, "Officer registered!");
        }

        new WelcomeFrame().setVisible(true);
        dispose();
    }
}
