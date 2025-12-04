package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collections;
import budgetsystem.model.Payment;

public class ReceiptFrame extends JFrame {

    // Old constructor kept for compatibility
    public ReceiptFrame(Payment payment) {
        this(Collections.singletonList(payment));
    }

    // New constructor for multiple payments
    public ReceiptFrame(List<Payment> payments) {
        setTitle("Official Receipt");
        setSize(500, 400);
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

        sb.append(String.format("%-25s %-10s %-15s %-12s\n",
                "Event", "Amount", "Officer", "Date"));
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

        sb.append("\nTOTAL PAID: ₱").append(String.format("%.2f", total)).append("\n");

        ta.setText(sb.toString());
        add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel p = new JPanel();
        p.setOpaque(false);
        JButton ok = new JButton("OK");
        p.add(ok);
        add(p, BorderLayout.SOUTH);

        ok.addActionListener(e -> dispose());
    }
}