package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        // BOTTOM BUTTONS (2 rows x 4 columns so all buttons are visible)
        JPanel buttons = new JPanel(new GridLayout(2, 4, 5, 5));

        JButton approveBtn       = new JButton("Approve Selected");
        JButton rejectBtn        = new JButton("Reject Selected");
        JButton viewPayments     = new JButton("View Payments Table");
        JButton yearSummaryBtn   = new JButton("Year Summary");
        JButton daySummaryBtn    = new JButton("Daily Summary");
        JButton weeklyReportBtn  = new JButton("Weekly Report");
        JButton budgetBtn        = new JButton("Budget Monitor"); // opens BudgetMonitorFrame
        JButton logout           = new JButton("Logout");

        buttons.add(approveBtn);
        buttons.add(rejectBtn);
        buttons.add(viewPayments);
        buttons.add(yearSummaryBtn);
        buttons.add(daySummaryBtn);
        buttons.add(weeklyReportBtn);
        buttons.add(budgetBtn);
        buttons.add(logout);

        add(buttons, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> changeProposalStatus(true));
        rejectBtn.addActionListener(e -> changeProposalStatus(false));
        viewPayments.addActionListener(e -> showPaymentsTable());
        yearSummaryBtn.addActionListener(e -> showSummaryByYearLevel());
        daySummaryBtn.addActionListener(e -> askAndShowDailySummary());
        weeklyReportBtn.addActionListener(e -> generateWeeklyReport());
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

    // ===========================================================
    // REPORTS
    // ===========================================================

    // 1) Summary by Year Level (all payments)
    private void showSummaryByYearLevel() {
        List<Payment> payments = LocalDatabase.payments;

        Map<String, Integer> countMap  = new TreeMap<>();
        Map<String, Double>  amountMap = new TreeMap<>();

        for (Payment p : payments) {
            String yearLevel = "N/A";
            for (Student s : LocalDatabase.students) {
                if (s.getId().equals(p.getStudentId())) {
                    yearLevel = s.getYearLevel();
                    break;
                }
            }

            countMap.put(yearLevel, countMap.getOrDefault(yearLevel, 0) + 1);
            amountMap.put(yearLevel, amountMap.getOrDefault(yearLevel, 0.0) + p.getAmount());
        }

        String[] cols = {"Year Level", "No. of Payments", "Total Amount"};
        Object[][] data = new Object[countMap.size()][cols.length];

        int i = 0;
        for (String year : countMap.keySet()) {
            data[i][0] = year;
            data[i][1] = countMap.get(year);
            data[i][2] = amountMap.get(year);
            i++;
        }

        JTable t = new JTable(new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        t.setAutoCreateRowSorter(true);

        JScrollPane sp = new JScrollPane(t);
        JOptionPane.showMessageDialog(this, sp,
                "Summary by Year Level", JOptionPane.INFORMATION_MESSAGE);
    }

    // 2) Ask for date range, then show daily summary
    private void askAndShowDailySummary() {
        String startStr = JOptionPane.showInputDialog(this, "Start date (YYYY-MM-DD):");
        if (startStr == null || startStr.isBlank()) return;

        String endStr = JOptionPane.showInputDialog(this, "End date (YYYY-MM-DD):");
        if (endStr == null || endStr.isBlank()) return;

        try {
            LocalDate start = LocalDate.parse(startStr.trim());
            LocalDate end   = LocalDate.parse(endStr.trim());
            showDailySummary(start, end);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
        }
    }

    private void showDailySummary(LocalDate start, LocalDate end) {
        List<Payment> payments = LocalDatabase.payments;

        Map<LocalDate, Integer> countMap  = new TreeMap<>();
        Map<LocalDate, Double>  amountMap = new TreeMap<>();

        for (Payment p : payments) {
            LocalDate d = p.getDate();
            if (d.isBefore(start) || d.isAfter(end)) continue;

            countMap.put(d, countMap.getOrDefault(d, 0) + 1);
            amountMap.put(d, amountMap.getOrDefault(d, 0.0) + p.getAmount());
        }

        String[] cols = {"Date", "No. of Payments", "Total Amount"};
        Object[][] data = new Object[countMap.size()][cols.length];

        int i = 0;
        for (LocalDate d : countMap.keySet()) {
            data[i][0] = d;
            data[i][1] = countMap.get(d);
            data[i][2] = amountMap.get(d);
            i++;
        }

        JTable t = new JTable(new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        t.setAutoCreateRowSorter(true);

        JScrollPane sp = new JScrollPane(t);
        JOptionPane.showMessageDialog(this, sp,
                "Daily Summary (" + start + " to " + end + ")",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // 3) Weekly report: current week (Mon–Sun), by day and year level
    private void generateWeeklyReport() {
        LocalDate today       = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek   = startOfWeek.plusDays(6);

        List<Payment> payments = LocalDatabase.payments;

        // per day
        Map<LocalDate, Integer> dayCount  = new TreeMap<>();
        Map<LocalDate, Double>  dayAmount = new TreeMap<>();

        // per year level
        Map<String, Integer> yearCount  = new TreeMap<>();
        Map<String, Double>  yearAmount = new TreeMap<>();

        for (Payment p : payments) {
            LocalDate d = p.getDate();
            if (d.isBefore(startOfWeek) || d.isAfter(endOfWeek)) continue;

            // by day
            dayCount.put(d, dayCount.getOrDefault(d, 0) + 1);
            dayAmount.put(d, dayAmount.getOrDefault(d, 0.0) + p.getAmount());

            // by year
            String yearLevel = "N/A";
            for (Student s : LocalDatabase.students) {
                if (s.getId().equals(p.getStudentId())) {
                    yearLevel = s.getYearLevel();
                    break;
                }
            }

            yearCount.put(yearLevel, yearCount.getOrDefault(yearLevel, 0) + 1);
            yearAmount.put(yearLevel, yearAmount.getOrDefault(yearLevel, 0.0) + p.getAmount());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WEEKLY REPORT\n");
        sb.append("From ").append(startOfWeek).append(" to ").append(endOfWeek).append("\n\n");

        sb.append("BY DAY:\n");
        for (LocalDate d : dayCount.keySet()) {
            sb.append(d)
              .append("  -  Payments: ").append(dayCount.get(d))
              .append("   Amount: ").append(String.format("%.2f", dayAmount.get(d)))
              .append("\n");
        }

        sb.append("\nBY YEAR LEVEL:\n");
        for (String y : yearCount.keySet()) {
            sb.append(y)
              .append("  -  Payments: ").append(yearCount.get(y))
              .append("   Amount: ").append(String.format("%.2f", yearAmount.get(y)))
              .append("\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 20, 60);
        area.setEditable(false);
        JScrollPane sp = new JScrollPane(area);

        JOptionPane.showMessageDialog(this, sp, "Weekly Report",
                JOptionPane.INFORMATION_MESSAGE);
    }
}