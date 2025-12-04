package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

        // TABLE MODEL WITH CHECKBOX + STATUS
        model = new DefaultTableModel(
                new Object[]{"Pay", "ID", "Title", "Amount", "Status", "Submitter"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                if (c != 0) return false;
                return getValueAt(r, 0) instanceof Boolean;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(1).setPreferredWidth(60); // ID column
        table.getColumnModel().getColumn(0)
                .setCellRenderer(new PayColumnRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton payBtn = new JButton("Pay Checked Events");
        JButton historyBtn = new JButton("View Payment History");
        JButton logout = new JButton("Logout");

        bottom.add(payBtn);
        bottom.add(historyBtn);
        bottom.add(logout);
        add(bottom, BorderLayout.SOUTH);

        payBtn.addActionListener(e -> paySelected());
        historyBtn.addActionListener(e -> viewHistory());
        logout.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });

        loadApprovedEvents();
    }

    // ============== RENDERER: hides checkbox when null =================
    private static class PayColumnRenderer extends JCheckBox implements TableCellRenderer {
        private final JLabel empty = new JLabel();

        PayColumnRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            empty.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            if (value instanceof Boolean) {
                setSelected((Boolean) value);
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                return this;
            } else {
                empty.setText("");
                if (isSelected) {
                    empty.setBackground(table.getSelectionBackground());
                    empty.setForeground(table.getSelectionForeground());
                } else {
                    empty.setBackground(table.getBackground());
                    empty.setForeground(table.getForeground());
                }
                return empty;
            }
        }
    }

    // ====================== LOAD APPROVED EVENTS =======================
    private void loadApprovedEvents() {
        model.setRowCount(0);

        for (Proposal p : LocalDatabase.proposals) {
            if (!p.getStatus().equalsIgnoreCase("Approved")) continue;

            boolean fullyPaid = isFullyPaidByStudent(p);
            Object payCell = fullyPaid ? null : Boolean.FALSE;
            String status = getPaymentStatus(p);
            double remaining = getRemainingForProposal(p);

            model.addRow(new Object[]{
                    payCell,
                    p.getProposalId(),
                    p.getTitle(),
                    "₱" + String.format("%.2f", remaining),
                    status,
                    p.getStudentId()
            });
        }
    }

    private double getTotalPaidForProposal(Proposal proposal) {
        double totalPaid = 0.0;
        for (Payment pay : LocalDatabase.payments) {
            if (pay.getStudentId().equals(student.getId())
                    && pay.getEventName().equals(proposal.getTitle())) {
                totalPaid += pay.getAmount();
            }
        }
        return totalPaid;
    }

    private double getRemainingForProposal(Proposal proposal) {
        double remaining = proposal.getAmount() - getTotalPaidForProposal(proposal);
        return remaining < 0 ? 0.0 : remaining;
    }

    private boolean isFullyPaidByStudent(Proposal proposal) {
        return getRemainingForProposal(proposal) <= 1e-6;
    }

    private String getPaymentStatus(Proposal proposal) {
        double totalPaid = getTotalPaidForProposal(proposal);
        double amount = proposal.getAmount();

        if (totalPaid <= 1e-6) {
            return "Unpaid";
        } else if (totalPaid + 1e-6 >= amount) {
            return "Paid in Full";
        } else {
            return String.format("Partial (₱%.2f / ₱%.2f)", totalPaid, amount);
        }
    }

    // ===================== PAY ALL CHECKED EVENTS ======================
    private void paySelected() {
        List<Integer> checkedRows = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 0);
            if (v instanceof Boolean && (Boolean) v) {
                checkedRows.add(i);
            }
        }

        if (checkedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Check at least one event first.");
            return;
        }

        int successCount = 0;
        List<Payment> paymentsMade = new ArrayList<>();

        for (int row : checkedRows) {
            String proposalId = (String) model.getValueAt(row, 1); // ID column
            Proposal chosen = null;

            for (Proposal p : LocalDatabase.proposals) {
                if (p.getProposalId().equals(proposalId)) {
                    chosen = p;
                    break;
                }
            }

            if (chosen == null) continue;
            if (isFullyPaidByStudent(chosen)) continue;

            Payment payment = processPaymentForProposal(chosen);
            if (payment != null) {
                successCount++;
                paymentsMade.add(payment);
            }
        }

        if (successCount > 0) {
            loadApprovedEvents(); // update status/amount/checkboxes
            new ReceiptFrame(paymentsMade).setVisible(true); // ONE receipt for all
            JOptionPane.showMessageDialog(this,
                    "Payments processed for " + successCount + " event(s).");
        }
    }

    // Returns the Payment made, or null if cancelled/invalid
    private Payment processPaymentForProposal(Proposal chosen) {
        double remaining = getRemainingForProposal(chosen);

        if (remaining <= 1e-6) {
            JOptionPane.showMessageDialog(this, "This event is already fully paid.");
            return null;
        }

        Object[] options = {
                "Pay Remaining (₱" + String.format("%.2f", remaining) + ")",
                "Pay Partial",
                "Cancel"
        };

        int option = JOptionPane.showOptionDialog(
                this,
                "Event: " + chosen.getTitle()
                        + "\nRemaining balance: ₱" + String.format("%.2f", remaining),
                "Payment",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );

        if (option == JOptionPane.CLOSED_OPTION || option == 2) {
            return null;
        }

        double payAmount;

        if (option == 0) { // pay remaining
            payAmount = remaining;
        } else { // partial
            String amt = JOptionPane.showInputDialog(
                    this,
                    "Enter amount for '" + chosen.getTitle()
                            + "' (max ₱" + String.format("%.2f", remaining) + "):"
            );
            if (amt == null) return null;

            try {
                payAmount = Double.parseDouble(amt);
                if (payAmount <= 0 || payAmount - remaining > 1e-6) {
                    throw new Exception();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid amount. It must be > 0 and ≤ remaining balance (₱"
                                + String.format("%.2f", remaining) + ").");
                return null;
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
        return payment;
    }

    // ======================= PAYMENT HISTORY ===========================
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