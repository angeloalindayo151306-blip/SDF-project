package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;

public class StudentFrame extends JFrame {
    private Student student;
    private JTable table;
    private DefaultTableModel model;

    public StudentFrame(Student s) {
        this.student = s;

        setTitle("Student - " + s.getFullName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Student Dashboard - " + s.getFullName(), SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Amount", "Submitter"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(60); // small ID
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton payBtn = new JButton("Pay Selected Event");
        JButton historyBtn = new JButton("View Payment History");
        JButton logout = new JButton("Logout");

        bottom.add(payBtn);
        bottom.add(historyBtn);
        bottom.add(logout);
        add(bottom, BorderLayout.SOUTH);

        payBtn.addActionListener(e -> paySelected());
        historyBtn.addActionListener(e -> viewHistory());
        logout.addActionListener(e -> { new WelcomeFrame().setVisible(true); dispose(); });

        loadApprovedEvents();
    }

    private void loadApprovedEvents() {
        model.setRowCount(0);

        for (Proposal p : LocalDatabase.proposals) {
            if (p.getStatus().equalsIgnoreCase("Approved")) {
                model.addRow(new Object[]{
                        p.getProposalId(),
                        p.getTitle(),
                        "₱" + String.format("%.2f", p.getAmount()),
                        p.getStudentId()
                });
            }
        }
    }

    private void paySelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }

        String proposalId = (String) model.getValueAt(row, 0);
        Proposal chosen = null;

        for (Proposal p : LocalDatabase.proposals) {
            if (p.getProposalId().equals(proposalId)) {
                chosen = p;
                break;
            }
        }

        if (chosen == null) return;

        Object[] options = {"Pay Full (₱" + chosen.getAmount() + ")", "Pay Partial"};
        int option = JOptionPane.showOptionDialog(
                this,
                "Choose payment option",
                "Payment",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );

        double payAmount;

        if (option == 0) {
            payAmount = chosen.getAmount();
        } else {
            String amt = JOptionPane.showInputDialog(this, "Enter amount:");
            if (amt == null) return;

            try {
                payAmount = Double.parseDouble(amt);
                if (payAmount <= 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
                return;
            }
        }

        Payment payment = new Payment(
                student.getFullName(),
                student.getId(),
                chosen.getTitle(),
                payAmount,
                chosen.getStudentId()
        );

        LocalDatabase.addPayment(payment);
        new ReceiptFrame(payment).setVisible(true);

        JOptionPane.showMessageDialog(this, "Payment successful.");
    }

    private void viewHistory() {
        StringBuilder sb = new StringBuilder();

        for (Payment p : LocalDatabase.payments) {
            if (p.getStudentId().equals(student.getId())) {
                sb.append(String.format("%s | %s | ₱%.2f | %s\n",
                        p.getEventName(), p.getDate(), p.getAmount(), p.getOfficerName()));
            }
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Payment History", JOptionPane.INFORMATION_MESSAGE);
    }
}
