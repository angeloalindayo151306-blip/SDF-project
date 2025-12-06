package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import budgetsystem.model.Payment;
import budgetsystem.model.Student;
import budgetsystem.model.Proposal;
import budgetsystem.util.LocalDatabase;

public class StudentFrame extends JFrame {
    private final Student student;
    private JTable tblPayments;
    private DefaultTableModel tableModel;
    private List<Payment> paymentsForStudent = new ArrayList<>();
    private JLabel lblStatusSummary;   // shows PAID / PARTIAL + remaining categories

    public StudentFrame(Student s) {
        this.student = s;

        setTitle("Student - " + s.getFullName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Student Dashboard - " + s.getFullName(),
                                   SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // Table model: read‑only
        tableModel = new DefaultTableModel(
                new Object[]{"Event", "Amount", "Officer", "Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // STATIC: no editing
            }
        };

        tblPayments = new JTable(tableModel);
        tblPayments.setRowHeight(25);
        tblPayments.setAutoCreateRowSorter(true);
        add(new JScrollPane(tblPayments), BorderLayout.CENTER);

        // Bottom panel: status label + buttons
        lblStatusSummary = new JLabel(" ");
        lblStatusSummary.setHorizontalAlignment(SwingConstants.LEFT);

        JButton btnReceipt = new JButton("View Receipt");
        JButton btnLogout  = new JButton("Logout");

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnReceipt);
        btnPanel.add(btnLogout);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(lblStatusSummary, BorderLayout.CENTER);
        bottom.add(btnPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        btnReceipt.addActionListener(e -> showReceipt());
        btnLogout.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });

        loadPayments();
    }

    // Load this student's payments into the table
    private void loadPayments() {
        paymentsForStudent.clear();
        tableModel.setRowCount(0);

        for (Payment p : LocalDatabase.payments) {
            if (p.getStudentId().equals(student.getId())) {
                paymentsForStudent.add(p);
                tableModel.addRow(new Object[]{
                        p.getEventName(),
                        String.format("₱%.2f", p.getAmount()),
                        p.getOfficerName(),
                        p.getDate().toString()
                });
            }
        }

        updateStatusSummary();
    }

    // Compute if student is fully paid or partial and list remaining categories
    private void updateStatusSummary() {
        StringBuilder remainingSb = new StringBuilder();
        boolean anyApproved = false;
        boolean anyRemaining = false;

        for (Proposal prop : LocalDatabase.proposals) {
            if (!"Approved".equalsIgnoreCase(prop.getStatus())) continue;
            anyApproved = true;

            double required = prop.getAmount();
            double paidTotal = 0.0;

            for (Payment pay : LocalDatabase.payments) {
                if (student.getId().equals(pay.getStudentId())
                        && prop.getTitle().equalsIgnoreCase(pay.getEventName())) {
                    paidTotal += pay.getAmount();
                }
            }

            if (paidTotal + 1e-6 < required) {
                anyRemaining = true;
                double remaining = required - paidTotal;
                if (remainingSb.length() > 0) remainingSb.append("; ");
                remainingSb.append(prop.getTitle())
                           .append(" (₱")
                           .append(String.format("%.2f", remaining))
                           .append(")");
            }
        }

        if (!anyApproved) {
            lblStatusSummary.setText("No approved events yet.");
            return;
        }

        if (!anyRemaining) {
            lblStatusSummary.setText("Status: PAID – all categories/events are fully paid.");
        } else {
            lblStatusSummary.setText(
                    "<html>Status: PARTIAL – remaining: " + remainingSb + "</html>");
        }
    }

    // Open receipt window for selected payment
    private void showReceipt() {
        int viewRow = tblPayments.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment first.");
            return;
        }

        int modelRow = tblPayments.convertRowIndexToModel(viewRow);
        Payment selected = paymentsForStudent.get(modelRow);

        // ReceiptFrame expects a List<Payment>
        List<Payment> list = new ArrayList<>();
        list.add(selected);

        new ReceiptFrame(list).setVisible(true);
    }
}