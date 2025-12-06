package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import budgetsystem.model.Payment;
import budgetsystem.model.Proposal;
import budgetsystem.util.LocalDatabase;

public class ReceiptFrame extends JFrame {

    // Old constructor kept for compatibility
    public ReceiptFrame(Payment payment) {
        this(Collections.singletonList(payment));
    }

    // New constructor for multiple payments
    public ReceiptFrame(List<Payment> payments) {
        setTitle("Official Receipt");
        setSize(600, 480);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(new Color(20, 20, 20));
        setLayout(new BorderLayout());

        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(30, 30, 30));
        ta.setForeground(Color.WHITE);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("OFFICIAL RECEIPT\n");
        sb.append("================\n\n");

        if (!payments.isEmpty()) {
            Payment first = payments.get(0);
            sb.append("Student : ").append(first.getStudentName()).append("\n");
            sb.append("ID      : ").append(first.getStudentId()).append("\n\n");
        }

        // ----------------- PAYMENT DETAILS -----------------
        sb.append("PAYMENT DETAILS\n");
        sb.append(String.format("%-25s %-10s %-15s %-12s\n",
                "Event / Category", "Amount", "Officer", "Date"));
        sb.append("-------------------------------------------------------------\n");

        double total = 0.0;
        for (Payment p : payments) {
            sb.append(String.format("%-25s ₱%8.2f %-15s %-12s\n",
                    p.getEventName(),
                    p.getAmount(),
                    p.getOfficerName(),
                    p.getDate().toString()));
            total += p.getAmount();
        }

        sb.append("\nTOTAL PAID THIS TRANSACTION: ₱")
          .append(String.format("%.2f", total)).append("\n");

        // ----------------- REMAINING CATEGORIES -----------------
        if (!payments.isEmpty()) {
            Payment first = payments.get(0);
            String studentId = first.getStudentId();

            StringBuilder remainingSb = new StringBuilder();
            boolean anyRemaining = false;

            for (Proposal prop : LocalDatabase.proposals) {
                if (!"Approved".equalsIgnoreCase(prop.getStatus())) continue;

                double requiredForEvent = prop.getAmount(); // per student
                double paidForEvent = 0.0;

                // sum all payments this student made for this event
                for (Payment pay : LocalDatabase.payments) {
                    if (studentId.equals(pay.getStudentId())
                            && prop.getTitle().equalsIgnoreCase(pay.getEventName())) {
                        paidForEvent += pay.getAmount();
                    }
                }

                if (paidForEvent + 1e-6 >= requiredForEvent) {
                    // fully paid event: no remaining categories
                    continue;
                }

                // Try to compute remaining per category from CATEGORY BREAKDOWN
                List<Category> categories = parseCategories(prop);
                if (categories.isEmpty()) {
                    // no breakdown; show event-level remaining
                    double remaining = requiredForEvent - paidForEvent;
                    anyRemaining = true;
                    remainingSb.append(String.format(" - %s  Remaining: ₱%.2f (Paid ₱%.2f of ₱%.2f)\n",
                            prop.getTitle(), remaining, paidForEvent, requiredForEvent));
                } else {
                    // distribute paid amount over categories in order
                    double remainingToCover = paidForEvent;
                    for (Category c : categories) {
                        double catPaid = Math.min(remainingToCover, c.amount);
                        remainingToCover -= catPaid;
                        double catRemaining = c.amount - catPaid;
                        if (catRemaining > 1e-6) {
                            anyRemaining = true;
                            remainingSb.append(String.format(
                                    " - %s - %s  Remaining: ₱%.2f (Category total ₱%.2f)\n",
                                    prop.getTitle(), c.name,
                                    catRemaining, c.amount));
                        }
                        if (remainingToCover <= 1e-6) {
                            // no more paid amount to distribute
                            remainingToCover = 0;
                        }
                    }
                }
            }

            sb.append("\n");

            if (anyRemaining) {
                sb.append("REMAINING CATEGORIES (NOT YET FULLY PAID):\n");
                sb.append(remainingSb);
                sb.append("\nOVERALL STATUS: PARTIAL PAYMENT\n");
            } else {
                sb.append("REMAINING CATEGORIES: None (All categories PAID)\n");
                sb.append("OVERALL STATUS: FULL PAYMENT (PAID)\n");
            }
        }

        ta.setText(sb.toString());
        add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel p = new JPanel();
        p.setOpaque(false);
        JButton ok = new JButton("OK");
        p.add(ok);
        add(p, BorderLayout.SOUTH);

        ok.addActionListener(e -> dispose());
    }

    // -----------------------------------------------------
    // Helper class + parser for category breakdown
    // -----------------------------------------------------
    private static class Category {
        String name;
        double amount;
    }

    /**
     * Parse categories from Proposal.description.
     * Format created in OfficerFrame.createProposal():
     *
     * CATEGORY BREAKDOWN:
     *  - registration fee: ₱50.00
     *  - snacks: ₱100.00
     *  - palaro fee: ₱500.00
     */
    private List<Category> parseCategories(Proposal p) {
        List<Category> out = new ArrayList<>();
        String desc = p.getDescription();
        if (desc == null) return out;

        String marker = "CATEGORY BREAKDOWN:";
        int idx = desc.indexOf(marker);
        if (idx < 0) return out;

        int notesIdx = desc.indexOf("\nNOTES:", idx);
        String block = (notesIdx >= 0)
                ? desc.substring(idx + marker.length(), notesIdx)
                : desc.substring(idx + marker.length());

        String[] lines = block.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("-")) continue;      // only "- category: ₱amount"
            line = line.substring(1).trim();          // remove leading "-"

            int colonIdx = line.indexOf(':');
            if (colonIdx < 0) continue;

            String catName = line.substring(0, colonIdx).trim();
            String amountPart = line.substring(colonIdx + 1).trim(); // "₱50.00"

            amountPart = amountPart.replace("₱", "").trim();
            try {
                double amt = Double.parseDouble(amountPart);
                Category c = new Category();
                c.name = catName;
                c.amount = amt;
                out.add(c);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }
}