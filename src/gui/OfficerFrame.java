package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;
import java.util.UUID;

public class OfficerFrame extends JFrame {
    private Officer officer;
    private JTextArea proposalsArea;

    public OfficerFrame(Officer officer) {
        this.officer = officer;
        setTitle("Officer - " + officer.getFullName());
        setSize(820, 560);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Officer Dashboard - " + officer.getPosition() + " " + officer.getFullName(), SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        add(header, BorderLayout.NORTH);

        proposalsArea = new JTextArea();
        proposalsArea.setEditable(false);
        add(new JScrollPane(proposalsArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton newProp = new JButton("Create Proposal");
        JButton viewPayments = new JButton("View All Payments");
        JButton logout = new JButton("Logout");
        bottom.add(newProp); bottom.add(viewPayments); bottom.add(logout);
        add(bottom, BorderLayout.SOUTH);

        newProp.addActionListener(e -> createProposal());
        viewPayments.addActionListener(e -> showPayments());
        logout.addActionListener(e -> { new WelcomeFrame().setVisible(true); dispose(); });

        updateProposalsDisplay();
    }

    private void updateProposalsDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("All proposals:\n\n");
        for (Proposal p : LocalDatabase.proposals) {
            sb.append(String.format("id=%s | title=%s | ₱%.2f | %s | submitter=%s\n",
                    p.getProposalId(), p.getTitle(), p.getAmount(), p.getStatus(), p.getStudentId()));
        }
        proposalsArea.setText(sb.toString());
    }

    private void createProposal() {
        JTextField title = new JTextField();
        JTextArea desc = new JTextArea(5, 30);
        JTextField amount = new JTextField();

        int ok = JOptionPane.showConfirmDialog(this, new Object[]{
                "Title:", title,
                "Description:", new JScrollPane(desc),
                "Amount (numbers only):", amount
        }, "New Proposal", JOptionPane.OK_CANCEL_OPTION);

        if (ok == JOptionPane.OK_OPTION) {
            try {
                double amt = Double.parseDouble(amount.getText().trim());
                String id = UUID.randomUUID().toString();
                // use officer id as submitter (studentId field in Proposal)
                Proposal p = new Proposal(id, officer.getId(), title.getText().trim(), desc.getText().trim(), amt, "Pending");
                LocalDatabase.addProposal(p);
                JOptionPane.showMessageDialog(this, "Proposal submitted (Pending).");
                updateProposalsDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            }
        }
    }

    private void showPayments() {
        StringBuilder sb = new StringBuilder();
        for (Payment p : LocalDatabase.payments) {
            sb.append(String.format("%s | %s | %s | ₱%.2f | %s\n",
                    p.getStudentName(), p.getStudentId(), p.getEventName(), p.getAmount(), p.getOfficerName()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "All Payments", JOptionPane.INFORMATION_MESSAGE);
    }
}
