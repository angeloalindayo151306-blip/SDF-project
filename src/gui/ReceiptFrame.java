package budgetsystem.gui;

import javax.swing.*;
import java.awt.*;
import budgetsystem.model.Payment;

public class ReceiptFrame extends JFrame {
    public ReceiptFrame(Payment payment) {
        setTitle("Official Receipt");
        setSize(420, 360);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(new Color(20,20,20));
        setLayout(new BorderLayout());

        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(30,30,30));
        ta.setForeground(Color.WHITE);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setText(payment.toCSV().replace(",", "\n").replace("Payment", "OFFICIAL RECEIPT"));
        add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel p = new JPanel();
        p.setOpaque(false);
        JButton ok = new JButton("OK");
        p.add(ok);
        add(p, BorderLayout.SOUTH);

        ok.addActionListener(e -> dispose());
    }
}