package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.UUID;
import budgetsystem.model.Proposal;
import budgetsystem.model.Officer;
import budgetsystem.util.LocalDatabase;

public class OfficerFrame extends JFrame {
    private Officer officer;
    private JTable table;
    private DefaultTableModel model;

    public OfficerFrame(Officer officer) {
        this.officer = officer;
        setTitle("Officer - " + officer.getFullName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Officer Dashboard - " + officer.getPosition() + " " + officer.getFullName(),
                SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // Columns: small ID column, Title, Per-student amount, Status
        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Per Student Amount", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        // make ID visually small
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton newProp = new JButton("Create Proposal");
        JButton viewPayments = new JButton("View All Payments");
        JButton logout = new JButton("Logout");

        bottom.add(newProp);
        bottom.add(viewPayments);
        bottom.add(logout);

        add(bottom, BorderLayout.SOUTH);

        newProp.addActionListener(e -> createProposal());
        viewPayments.addActionListener(e -> showPayments());
        logout.addActionListener(e -> { new WelcomeFrame().setVisible(true); dispose(); });

        loadProposals();
    }

    private void loadProposals() {
        model.setRowCount(0);

        for (Proposal p : LocalDatabase.proposals) {
            // Show proposals created by this officer (submitter stored in studentId field)
            if (p.getStudentId().equals(officer.getId())) {
                model.addRow(new Object[]{
                        p.getProposalId(),
                        p.getTitle(),
                        "₱" + String.format("%.2f", p.getAmount()), // amount stored is per-student (per your workflow)
                        p.getStatus()
                });
            }
        }
    }

    private void createProposal() {
        JTextField title = new JTextField();
        JTextArea desc = new JTextArea(5, 30);
        JTextField totalAmount = new JTextField();
        JTextField numStudents = new JTextField();

        int ok = JOptionPane.showConfirmDialog(this, new Object[]{
                "Title:", title,
                "Description:", new JScrollPane(desc),
                "Total Amount (₱):", totalAmount,
                "Number of students who will pay:", numStudents
        }, "New Proposal", JOptionPane.OK_CANCEL_OPTION);

        if (ok == JOptionPane.OK_OPTION) {
            try {
                double total = Double.parseDouble(totalAmount.getText().trim());
                int students = Integer.parseInt(numStudents.getText().trim());
                if (students <= 0) {
                    JOptionPane.showMessageDialog(this, "Number of students must be positive.");
                    return;
                }

                double perStudent = total / students;
                String id = UUID.randomUUID().toString(); // or use short id generator if you have one

                // NOTE: This matches your Proposal constructor:
                // Proposal(String proposalId, String studentId, String title, String description, double amount, int numberOfStudents, String status)
                Proposal p = new Proposal(
                        id,
                        officer.getId(),
                        title.getText().trim(),
                        desc.getText().trim(),
                        perStudent,
                        students,
                        "Pending"
                );

                LocalDatabase.addProposal(p);
                JOptionPane.showMessageDialog(this, "Proposal submitted (Pending).");
                loadProposals();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating proposal: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showPayments() {
        StringBuilder sb = new StringBuilder();
        for (budgetsystem.model.Payment p : LocalDatabase.payments) {
            sb.append(String.format("%s | %s | %s | ₱%.2f | %s\n",
                    p.getStudentName(), p.getStudentId(), p.getEventName(), p.getAmount(), p.getOfficerName()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "All Payments", JOptionPane.INFORMATION_MESSAGE);
    }
}
