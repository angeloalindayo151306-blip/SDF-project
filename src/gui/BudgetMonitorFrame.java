package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import budgetsystem.model.Payment;
import budgetsystem.model.Proposal;
import budgetsystem.util.LocalDatabase;

public class BudgetMonitorFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JLabel totalAllocatedLbl;
    private JLabel totalCollectedLbl;
    private JLabel overallBalanceLbl;

    public BudgetMonitorFrame() {
        setTitle("Budget Monitoring & Allocation");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Budget Monitoring & Allocation", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Event", "Allocated Budget", "Collected (Payments)",
                        "Remaining", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom summary panel
        JPanel bottom = new JPanel(new GridLayout(2, 3, 10, 4));

        totalAllocatedLbl = new JLabel("₱0.00");
        totalCollectedLbl = new JLabel("₱0.00");
        overallBalanceLbl = new JLabel("Overall Balance: ₱0.00", SwingConstants.RIGHT);

        bottom.add(new JLabel("Total Allocated:", SwingConstants.RIGHT));
        bottom.add(totalAllocatedLbl);
        bottom.add(new JLabel("")); // spacer

        bottom.add(new JLabel("Total Collected:", SwingConstants.RIGHT));
        bottom.add(totalCollectedLbl);
        bottom.add(overallBalanceLbl);

        add(bottom, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        double totalAllocated = 0;
        double totalCollected = 0;

        for (Proposal p : LocalDatabase.proposals) {
            if (!p.getStatus().equalsIgnoreCase("Approved")) continue;

            double allocated = p.getAmount();
            double collected = 0;

            for (Payment pay : LocalDatabase.payments) {
                if (pay.getEventName().equals(p.getTitle())) {
                    collected += pay.getAmount();
                }
            }

            double remaining = allocated - collected;
            String status;
            if (remaining > 0.01)      status = "Underfunded";
            else if (remaining < -0.01) status = "Surplus";
            else                        status = "Balanced";

            model.addRow(new Object[]{
                    p.getTitle(),
                    String.format("₱%.2f", allocated),
                    String.format("₱%.2f", collected),
                    String.format("₱%.2f", remaining),
                    status
            });

            totalAllocated += allocated;
            totalCollected += collected;
        }

        double balance = totalCollected - totalAllocated;

        totalAllocatedLbl.setText(String.format("₱%.2f", totalAllocated));
        totalCollectedLbl.setText(String.format("₱%.2f", totalCollected));
        overallBalanceLbl.setText("Overall Balance: " + String.format("₱%.2f", balance));
    }
}