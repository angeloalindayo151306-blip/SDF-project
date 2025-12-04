package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;

public class DeanFrame extends JFrame {
    private Dean dean;
    private JTable table;
    private DefaultTableModel tableModel;

    public DeanFrame(Dean dean) {
        this.dean = dean;
        setTitle("Dean Dashboard - " + dean.getFullName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Dean Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // TABLE MODEL — FIRST COLUMN IS CHECKBOX (ONLY FOR PENDING)
        tableModel = new DefaultTableModel(
                new Object[]{"Approve", "ID", "Title", "Total Amount", "Status", "Submitter"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                // Checkbox only editable if Pending and actually has Boolean value
                if (col == 0) {
                    Object v = getValueAt(row, 0);
                    if (!(v instanceof Boolean)) return false;
                    String status = (String) getValueAt(row, 4);
                    return status.equalsIgnoreCase("Pending");
                }
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);

        // Use a custom renderer so checkbox disappears when not Pending
        table.getColumnModel().getColumn(0)
             .setCellRenderer(new ApproveColumnRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);

        // BOTTOM BUTTONS
        JPanel buttons = new JPanel();
        JButton approveBtn   = new JButton("Approve Selected");
        JButton rejectBtn    = new JButton("Reject Selected");
        JButton viewPayments = new JButton("View Payments Table");
        JButton budgetBtn    = new JButton("Budget Monitor"); // opens BudgetMonitorFrame
        JButton logout       = new JButton("Logout");

        buttons.add(approveBtn);
        buttons.add(rejectBtn);
        buttons.add(viewPayments);
        buttons.add(budgetBtn);
        buttons.add(logout);
        add(buttons, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> changeProposalStatus(true));
        rejectBtn.addActionListener(e -> changeProposalStatus(false));
        viewPayments.addActionListener(e -> showPaymentsTable());
        budgetBtn.addActionListener(e -> new BudgetMonitorFrame().setVisible(true));
        logout.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });

        loadProposalsToTable();
    }

    // Renderer for the "Approve" column: show checkbox only when status is Pending
    private class ApproveColumnRenderer extends JCheckBox implements TableCellRenderer {
        private final JLabel empty = new JLabel();

        ApproveColumnRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            empty.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            String status = (String) table.getValueAt(row, 4); // col 4 = Status

            if ("Pending".equalsIgnoreCase(status) && value instanceof Boolean) {
                setSelected(Boolean.TRUE.equals(value));
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

    // ===========================================================
    // LOAD PROPOSALS — only PENDING shows checkbox
    // Amount = FULL event amount (same as Officer & BudgetMonitor)
    // ===========================================================
    private void loadProposalsToTable() {
        tableModel.setRowCount(0);

        for (Proposal p : LocalDatabase.proposals) {

            Object checkValue =
                    p.getStatus().equalsIgnoreCase("Pending")
                            ? Boolean.FALSE   // checkbox visible
                            : null;           // hidden by renderer

            tableModel.addRow(new Object[]{
                    checkValue,
                    p.getProposalId(),
                    p.getTitle(),
                    String.format("₱%.2f", p.getAmount()), // FULL total amount
                    p.getStatus(),
                    p.getStudentId()
            });
        }
    }

    private void changeProposalStatus(boolean approve) {
        boolean found = false;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 0);

            // Skip rows that have no checkbox (Approved/Rejected)
            if (!(val instanceof Boolean)) continue;

            boolean checked = (boolean) val;

            if (checked) {
                String id = (String) tableModel.getValueAt(i, 1);

                for (Proposal p : LocalDatabase.proposals) {
                    if (p.getProposalId().equals(id)) {
                        p.setStatus(approve ? "Approved" : "Rejected");
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "No checked proposals.");
            return;
        }

        LocalDatabase.updateProposalsFile();
        loadProposalsToTable();
        JOptionPane.showMessageDialog(this, "Selected proposals updated.");
    }

    // ===========================================================
    // PAYMENTS TABLE (with Year Level)
    // ===========================================================
    private void showPaymentsTable() {

        String[] cols = {
            "Student Name",
            "Year Level",
            "Student ID",
            "Event",
            "Amount",
            "Officer / Submitter",
            "Date"
        };

        Object[][] data = new Object[LocalDatabase.payments.size()][7];
        int r = 0;

        for (Payment p : LocalDatabase.payments) {

            String yearLevel = "N/A";

            for (Student s : LocalDatabase.students) {
                if (s.getId().equals(p.getStudentId())) {
                    yearLevel = s.getYearLevel();
                    break;
                }
            }

            data[r][0] = p.getStudentName();
            data[r][1] = yearLevel;
            data[r][2] = p.getStudentId();
            data[r][3] = p.getEventName();
            data[r][4] = p.getAmount();
            data[r][5] = p.getOfficerName();
            data[r][6] = p.getDate().toString();
            r++;
        }

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table.setAutoCreateRowSorter(true);
        TableRowSorter sorter = new TableRowSorter(table.getModel());
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        JOptionPane.showMessageDialog(this, sp, "Payments Table", JOptionPane.INFORMATION_MESSAGE);
    }
}