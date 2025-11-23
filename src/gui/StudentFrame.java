package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;

public class StudentFrame extends JFrame {
    private Student student;
    private JTextArea availableArea;

    public StudentFrame(Student s) {
        this.student = s;
        setTitle("Student - " + s.getFullName());
        setSize(820, 560);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Student Dashboard - " + s.getFullName(), SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        add(header, BorderLayout.NORTH);

        availableArea = new JTextArea();
        availableArea.setEditable(false);
        add(new JScrollPane(availableArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton payBtn = new JButton("Pay Selected Event");
        JButton historyBtn = new JButton("View My Payment History");
        JButton logout = new JButton("Logout");
        bottom.add(payBtn); bottom.add(historyBtn); bottom.add(logout);
        add(bottom, BorderLayout.SOUTH);

        payBtn.addActionListener(e -> paySelected());
        historyBtn.addActionListener(e -> viewHistory());
        logout.addActionListener(e -> { new WelcomeFrame().setVisible(true); dispose(); });

        updateAvailable();
    }

    private void updateAvailable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Approved events:\n\n");
        int i = 1;
        for (Proposal p : LocalDatabase.proposals) {
            if ("Approved".equalsIgnoreCase(p.getStatus())) {
                sb.append(String.format("[%d] id=%s | %s | ₱%.2f | submitter=%s\n",
                        i++, p.getProposalId(), p.getTitle(), p.getAmount(), p.getStudentId()));
            }
        }
        availableArea.setText(sb.toString());
    }

    private void paySelected() {
        java.util.List<Proposal> approved = LocalDatabase.getApprovedProposals();
        if (approved.isEmpty()) { JOptionPane.showMessageDialog(this, "No approved events available."); return; }

        String[] items = new String[approved.size()];
        for (int i = 0; i < approved.size(); i++) {
            Proposal p = approved.get(i);
            items[i] = p.getProposalId() + " — " + p.getTitle() + " (₱" + String.format("%.2f", p.getAmount()) + ")";
        }

        String selected = (String) JOptionPane.showInputDialog(this, "Select Event:", "Pay Event", JOptionPane.PLAIN_MESSAGE, null, items, items[0]);
        if (selected == null) return;

        int idx = -1;
        for (int i = 0; i < items.length; i++) if (items[i].equals(selected)) { idx = i; break; }
        if (idx == -1) return;
        Proposal chosen = approved.get(idx);

        Object[] options = {"Pay Full (₱" + String.format("%.2f", chosen.getAmount()) + ")", "Pay Partial"};
        int opt = JOptionPane.showOptionDialog(this, "Payment Type", "Payment", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        double amountPaid;
        if (opt == 0) {
            amountPaid = chosen.getAmount();
        } else {
            String amtStr = JOptionPane.showInputDialog(this, "Enter amount to pay:");
            if (amtStr == null) return;
            try {
                amountPaid = Double.parseDouble(amtStr);
                if (amountPaid <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
                return;
            }
        }

        // Use proposal.submitter as officer/submitter id in officerName field of Payment
        Payment pay = new Payment(student.getFullName(), student.getId(), chosen.getTitle(), amountPaid, chosen.getStudentId());
        LocalDatabase.addPayment(pay);
        new ReceiptFrame(pay).setVisible(true);
        JOptionPane.showMessageDialog(this, "Payment successful.");
        updateAvailable();
    }

    private void viewHistory() {
        StringBuilder sb = new StringBuilder();
        for (Payment p : LocalDatabase.payments) {
            if (p.getStudentId().equals(student.getId())) {
                sb.append(String.format("%s | %s | ₱%.2f | %s | %s\n", p.getDate(), p.getEventName(), p.getAmount(), p.getOfficerName(), p.getStudentName()));
            }
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Your Payment History", JOptionPane.INFORMATION_MESSAGE);
    }
}
