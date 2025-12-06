package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;

public class OfficerFrame extends JFrame {
    private Officer officer;
    private JTable table;
    private DefaultTableModel model;

    // filter for proposal table: ALL / ONGOING / DONE
    private String eventFilter = "ALL";

    public OfficerFrame(Officer officer) {
        this.officer = officer;
        setTitle("Officer - " + officer.getFullName());
        setSize(950, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        JLabel header = new JLabel(
                "Officer Dashboard - " + officer.getPosition() + " " + officer.getFullName(),
                SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // Columns: ID, Title, TOTAL event amount per student, Status
        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Total Amount", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel();

        JButton newProp          = new JButton("Create Proposal");
        JButton viewPayments     = new JButton("View All Payments");
        JButton studentStatusBtn = new JButton("Student Payment Status");
        JButton budgetBtn        = new JButton("Budget Monitor");
        JButton logout           = new JButton("Logout");

        JButton btnAllEvents     = new JButton("All Events");
        JButton btnOngoing       = new JButton("Ongoing");
        JButton btnDone          = new JButton("Done");

        bottom.add(newProp);
        bottom.add(viewPayments);
        bottom.add(studentStatusBtn);
        bottom.add(budgetBtn);
        bottom.add(btnAllEvents);
        bottom.add(btnOngoing);
        bottom.add(btnDone);
        bottom.add(logout);

        add(bottom, BorderLayout.SOUTH);

        newProp.addActionListener(e -> createProposal());
        viewPayments.addActionListener(e -> showPayments());
        studentStatusBtn.addActionListener(e -> openStudentStatusDialog());
        budgetBtn.addActionListener(e -> new BudgetMonitorFrame().setVisible(true));
        logout.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });

        btnAllEvents.addActionListener(e -> {
            eventFilter = "ALL";
            loadProposals();
        });
        btnOngoing.addActionListener(e -> {
            eventFilter = "ONGOING";
            loadProposals();
        });
        btnDone.addActionListener(e -> {
            eventFilter = "DONE";
            loadProposals();
        });

        loadProposals();
    }

    // ==========================================================
    // Load only proposals submitted by this officer (+ filter)
    // ==========================================================
    private void loadProposals() {
        model.setRowCount(0);
        LocalDate today = LocalDate.now();

        for (Proposal p : LocalDatabase.proposals) {
            if (!p.getStudentId().equals(officer.getId())) continue;

            if ("ONGOING".equals(eventFilter) && !p.isOngoing(today)) continue;
            if ("DONE".equals(eventFilter) && !p.isDone(today)) continue;

            model.addRow(new Object[]{
                    p.getProposalId(),
                    p.getTitle(),
                    "₱" + String.format("%.2f", p.getAmount()), // per-student amount
                    p.getStatus()
            });
        }
    }

    // ==========================================================
    // Create Proposal – with category table + auto total + dates
    // ==========================================================
    private void createProposal() {
        JTextField txtTitle = new JTextField();
        JTextArea txtDesc = new JTextArea(3, 30);

        // Table for categories (budget breakdown)
        DefaultTableModel catModel = new DefaultTableModel(
                new Object[]{"Category", "Amount"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Double.class : String.class;
            }
        };
        JTable catTable = new JTable(catModel);
        catTable.setRowHeight(22);

        JButton btnAddRow = new JButton("Add Category");
        JButton btnRemoveRow = new JButton("Remove Category");
        JLabel lblTotal = new JLabel("Total: ₱0.00");

        btnAddRow.addActionListener(e -> catModel.addRow(new Object[]{"", 0.0}));

        btnRemoveRow.addActionListener(e -> {
            int row = catTable.getSelectedRow();
            if (row >= 0) {
                catModel.removeRow(row);
                updateCategoryTotal(catModel, lblTotal);
            }
        });

        // Recalculate total whenever table changes
        catModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateCategoryTotal(catModel, lblTotal);
            }
        });

        JTextField txtNumStudents = new JTextField();

        // timeline fields
        JTextField txtStartDate = new JTextField("YYYY-MM-DD");
        JTextField txtEndDate   = new JTextField("YYYY-MM-DD");

        // Build panel for dialog
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        JPanel north = new JPanel(new BorderLayout(5, 5));
        north.add(new JLabel("Title:"), BorderLayout.WEST);
        north.add(txtTitle, BorderLayout.CENTER);

        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.add(new JLabel("Additional Notes / Description:"), BorderLayout.NORTH);
        descPanel.add(new JScrollPane(txtDesc), BorderLayout.CENTER);

        JPanel catTop = new JPanel(new BorderLayout(5, 5));
        catTop.add(new JLabel("Budget Categories (name + amount):"), BorderLayout.WEST);
        JPanel catButtons = new JPanel();
        catButtons.add(btnAddRow);
        catButtons.add(btnRemoveRow);
        catTop.add(catButtons, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(3, 3));
        center.add(catTop, BorderLayout.NORTH);
        center.add(new JScrollPane(catTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new GridLayout(3, 1, 5, 5));

        JPanel studentsPanel = new JPanel(new BorderLayout(5, 5));
        studentsPanel.add(new JLabel("Number of students who will pay:"), BorderLayout.WEST);
        studentsPanel.add(txtNumStudents, BorderLayout.CENTER);

        JPanel datePanel = new JPanel(new GridLayout(1, 4, 5, 5));
        datePanel.add(new JLabel("Start date (YYYY-MM-DD):"));
        datePanel.add(txtStartDate);
        datePanel.add(new JLabel("End date (YYYY-MM-DD):"));
        datePanel.add(txtEndDate);

        JPanel totalPanel = new JPanel(new BorderLayout(5, 5));
        totalPanel.add(lblTotal, BorderLayout.WEST);

        south.add(studentsPanel);
        south.add(datePanel);
        south.add(totalPanel);

        mainPanel.add(north, BorderLayout.NORTH);
        mainPanel.add(descPanel, BorderLayout.WEST);
        mainPanel.add(center, BorderLayout.CENTER);
        mainPanel.add(south, BorderLayout.SOUTH);

        int ok = JOptionPane.showConfirmDialog(
                this,
                mainPanel,
                "New Proposal / Event",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (ok == JOptionPane.OK_OPTION) {
            try {
                double total = computeCategoryTotal(catModel);
                if (total <= 0 || catModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Please add at least one category with a positive amount.");
                    return;
                }

                int students;
                try {
                    students = Integer.parseInt(txtNumStudents.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number of students.");
                    return;
                }
                if (students <= 0) {
                    JOptionPane.showMessageDialog(this, "Number of students must be positive.");
                    return;
                }

                String title = txtTitle.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title is required.");
                    return;
                }

                LocalDate startDate;
                LocalDate endDate;
                try {
                    startDate = LocalDate.parse(txtStartDate.getText().trim());
                    endDate   = LocalDate.parse(txtEndDate.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid date format. Use YYYY-MM-DD for start and end dates.");
                    return;
                }
                if (endDate.isBefore(startDate)) {
                    JOptionPane.showMessageDialog(this,
                            "End date cannot be before start date.");
                    return;
                }

                // description includes category breakdown
                StringBuilder desc = new StringBuilder();
                desc.append("CATEGORY BREAKDOWN:\n");
                for (int i = 0; i < catModel.getRowCount(); i++) {
                    String cat = String.valueOf(catModel.getValueAt(i, 0));
                    Object val = catModel.getValueAt(i, 1);
                    double amt = 0;
                    try {
                        amt = Double.parseDouble(String.valueOf(val));
                    } catch (Exception ignored) { }
                    desc.append(" - ").append(cat).append(": ₱")
                        .append(String.format("%.2f", amt)).append("\n");
                }
                desc.append("\nNOTES:\n").append(txtDesc.getText().trim());

                String id = UUID.randomUUID().toString();

                Proposal p = new Proposal(
                        id,
                        officer.getId(),
                        title,
                        desc.toString(),
                        total,            // per-student fee
                        students,
                        "Pending",
                        startDate,
                        endDate
                );

                LocalDatabase.addProposal(p);
                JOptionPane.showMessageDialog(this,
                        "Proposal submitted (Pending).\nPer student: ₱"
                                + String.format("%.2f", total));

                loadProposals();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating proposal: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void updateCategoryTotal(DefaultTableModel catModel, JLabel lbl) {
        double total = computeCategoryTotal(catModel);
        lbl.setText("Total: ₱" + String.format("%.2f", total));
    }

    private double computeCategoryTotal(DefaultTableModel catModel) {
        double total = 0.0;
        for (int i = 0; i < catModel.getRowCount(); i++) {
            Object val = catModel.getValueAt(i, 1);
            if (val == null) continue;
            try {
                total += Double.parseDouble(val.toString());
            } catch (NumberFormatException ignored) {}
        }
        return total;
    }

    // ==========================================================
    // Student payment status (info + Full Pay + Partial Pay UI)
    // ==========================================================
    private void openStudentStatusDialog() {
        if (LocalDatabase.students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students in the database.");
            return;
        }
        StudentStatusDialog dlg = new StudentStatusDialog(this);
        dlg.setVisible(true);
    }

    private class StudentStatusDialog extends JDialog {
        private JComboBox<Student> cbStudents;
        private DefaultTableModel statusModel;
        private JLabel lblSummary;
        private JLabel lblStudentInfo;

        // List of proposals matching each row
        private List<Proposal> proposalRows = new ArrayList<>();

        StudentStatusDialog(Frame owner) {
            super(owner, "Student Payment Status", true);
            setSize(900, 480);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(5, 5));

            cbStudents = new JComboBox<>();
            for (Student s : LocalDatabase.students) {
                cbStudents.addItem(s);
            }
            cbStudents.addActionListener(e -> refreshTable());

            lblStudentInfo = new JLabel(" ");
            lblStudentInfo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            JPanel top = new JPanel(new BorderLayout(5, 5));
            JPanel topRow = new JPanel(new BorderLayout(5, 5));
            topRow.add(new JLabel("Select Student: "), BorderLayout.WEST);
            topRow.add(cbStudents, BorderLayout.CENTER);
            top.add(topRow, BorderLayout.NORTH);
            top.add(lblStudentInfo, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);

            // Table: information only (checkbox can be used as marker if you want)
            statusModel = new DefaultTableModel(
                    new Object[]{"Select", "Event / Category",
                            "Required", "Paid", "Remaining", "Status"}, 0
            ) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return c == 0; // only checkbox editable
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : String.class;
                }
            };

            JTable tbl = new JTable(statusModel);
            tbl.setRowHeight(22);
            add(new JScrollPane(tbl), BorderLayout.CENTER);

            lblSummary = new JLabel(" ");
            JButton btnFullPay    = new JButton("Full Pay (all remaining)");
            JButton btnPartialPay = new JButton("Partial Pay (select categories)");
            JButton btnClose      = new JButton("Close");

            btnFullPay.addActionListener(e -> handleFullPay());
            btnPartialPay.addActionListener(e -> handlePartialPay());
            btnClose.addActionListener(e -> dispose());

            JPanel bottom = new JPanel(new BorderLayout(5, 5));
            bottom.add(lblSummary, BorderLayout.CENTER);
            JPanel btnPanel = new JPanel();
            btnPanel.add(btnFullPay);
            btnPanel.add(btnPartialPay);
            btnPanel.add(btnClose);
            bottom.add(btnPanel, BorderLayout.EAST);

            add(bottom, BorderLayout.SOUTH);

            if (cbStudents.getItemCount() > 0) {
                cbStudents.setSelectedIndex(0);
                refreshTable();
            }
        }

        private double getRequiredAmount(Proposal p) {
            return p.getAmount(); // per student
        }

        private double getTotalPaidForProposal(Proposal p, Student s) {
            double totalPaid = 0.0;
            for (Payment pay : LocalDatabase.payments) {
                if (s.getId().equals(pay.getStudentId())
                        && pay.getEventName().equalsIgnoreCase(p.getTitle())) {
                    totalPaid += pay.getAmount();
                }
            }
            return totalPaid;
        }

        private String getStatusString(double required, double paid) {
            if (paid + 1e-6 >= required) {
                return "Paid";
            } else {
                return "Unpaid";
            }
        }

        // --------- FULL PAY (pay all remaining categories) ----------
        private void handleFullPay() {
            Student s = (Student) cbStudents.getSelectedItem();
            if (s == null) return;

            List<Payment> newPayments = new ArrayList<>();

            for (Proposal p : proposalRows) {
                double required  = getRequiredAmount(p);
                double paid      = getTotalPaidForProposal(p, s);
                double remaining = required - paid;
                if (remaining <= 1e-6) continue; // already fully paid

                Payment pay = new Payment(
                        s.getFullName(),
                        s.getId(),
                        p.getTitle(),
                        remaining,
                        officer.getFullName()
                );
                LocalDatabase.addPayment(pay);
                newPayments.add(pay);
            }

            if (newPayments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Student is already fully paid in all categories.");
                return;
            }

            refreshTable();
            new ReceiptFrame(newPayments).setVisible(true);
            JOptionPane.showMessageDialog(this,
                    "Full payment recorded. All categories are now Paid.");
        }

        // --------- PARTIAL PAY (open another UI with checkboxes) ----------
        private void handlePartialPay() {
            Student s = (Student) cbStudents.getSelectedItem();
            if (s == null) return;

            PartialPayDialog dlg = new PartialPayDialog(OfficerFrame.this, s);
            dlg.setVisible(true);

            // After partial payment, refresh the table to reflect new balances
            refreshTable();
        }

        // --------- Load ALL categories (Paid / Unpaid) ----------
        private void refreshTable() {
            Student s = (Student) cbStudents.getSelectedItem();
            if (s == null) return;

            // update student info
            lblStudentInfo.setText(
                    "ID: " + s.getId() +
                    "   Name: " + s.getFullName() +
                    "   Course: " + s.getCourse() +
                    "   Year: " + s.getYearLevel()
            );

            statusModel.setRowCount(0);
            proposalRows.clear();

            int totalCategories = 0;
            int paidCategories  = 0;

            for (Proposal p : LocalDatabase.proposals) {
                if (!"Approved".equalsIgnoreCase(p.getStatus())) continue;

                totalCategories++;

                double required  = getRequiredAmount(p);
                double paid      = getTotalPaidForProposal(p, s);
                double remaining = required - paid;
                if (remaining < 0) remaining = 0.0;

                boolean fullyPaid = remaining <= 1e-6;
                if (fullyPaid) paidCategories++;

                proposalRows.add(p);

                statusModel.addRow(new Object[]{
                        Boolean.FALSE,                     // Select checkbox (marker)
                        p.getTitle(),
                        String.format("₱%.2f", required),
                        String.format("₱%.2f", paid),
                        String.format("₱%.2f", remaining),
                        getStatusString(required, paid)    // "Paid" or "Unpaid"
                });
            }

            lblSummary.setText("Total categories: " + totalCategories
                    + "   |   Fully paid: " + paidCategories);
        }
    }

    // ==========================================================
    // PartialPayDialog – shows Category + Amount + Checkbox
    // ==========================================================
    private class PartialPayDialog extends JDialog {

        private final Student student;

        // Represents one selectable category row
        private class CategoryRow {
            Proposal proposal;
            String categoryName;
            double amount;
            JCheckBox checkBox;
        }

        private java.util.List<CategoryRow> rows = new java.util.ArrayList<>();

        PartialPayDialog(Frame owner, Student student) {
            super(owner, "Partial Payment (by Category)", true);
            this.student = student;

            setSize(550, 420);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(5, 5));

            JLabel info = new JLabel("Select categories to pay for student: "
                    + student.getFullName() + " (" + student.getId() + ")");
            info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(info, BorderLayout.NORTH);

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

            // Build list of categories (from proposal description)
            for (Proposal p : LocalDatabase.proposals) {
                if (!"Approved".equalsIgnoreCase(p.getStatus())) continue;

                java.util.List<CategoryRow> catRows = parseCategoriesForProposal(p);
                for (CategoryRow row : catRows) {
                    // Check remaining balance for the whole event
                    double requiredForEvent = p.getAmount();
                    double alreadyPaidForEvent = getTotalPaidForProposal(p, student);
                    double remainingForEvent = requiredForEvent - alreadyPaidForEvent;
                    if (remainingForEvent <= 1e-6) {
                        // Event already fully paid; skip all categories
                        continue;
                    }

                    // Cap category amount to remaining event balance for display
                    double effectiveAmount = Math.min(row.amount, remainingForEvent);

                    String text = String.format("%s - %s   (₱%.2f)",
                            p.getTitle(), row.categoryName, effectiveAmount);

                    JCheckBox cb = new JCheckBox(text);
                    row.checkBox = cb;
                    rows.add(row);
                    listPanel.add(cb);
                }
            }

            if (rows.isEmpty()) {
                listPanel.add(new JLabel("No remaining categories to pay. Student is fully paid."));
            }

            JScrollPane sp = new JScrollPane(listPanel);
            add(sp, BorderLayout.CENTER);

            JButton btnPay    = new JButton("Pay Selected");
            JButton btnCancel = new JButton("Cancel");

            btnPay.addActionListener(e -> doPay());
            btnCancel.addActionListener(e -> dispose());

            JPanel bottom = new JPanel();
            bottom.add(btnPay);
            bottom.add(btnCancel);
            add(bottom, BorderLayout.SOUTH);
        }

        // Parse "CATEGORY BREAKDOWN" from proposal.description
        // Format created in createProposal():
        // CATEGORY BREAKDOWN:
        //  - registration fee: ₱50.00
        //  - snacks: ₱100.00
        //  - palaro fee: ₱500.00
        private java.util.List<CategoryRow> parseCategoriesForProposal(Proposal p) {
            java.util.List<CategoryRow> out = new java.util.ArrayList<>();
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
                if (!line.startsWith("-")) continue;       // only "- category: ₱amount"
                line = line.substring(1).trim();           // remove leading "-"

                int colonIdx = line.indexOf(':');
                if (colonIdx < 0) continue;

                String catName = line.substring(0, colonIdx).trim();
                String amountPart = line.substring(colonIdx + 1).trim(); // "₱50.00"

                amountPart = amountPart.replace("₱", "").trim();
                try {
                    double amt = Double.parseDouble(amountPart);
                    CategoryRow row = new CategoryRow();
                    row.proposal = p;
                    row.categoryName = catName;
                    row.amount = amt;
                    out.add(row);
                } catch (NumberFormatException ignored) {
                }
            }
            return out;
        }

        private double getTotalPaidForProposal(Proposal p, Student s) {
            double totalPaid = 0.0;
            String eventTitle = p.getTitle();
            for (Payment pay : LocalDatabase.payments) {
                if (s.getId().equals(pay.getStudentId())
                        && pay.getEventName().equalsIgnoreCase(eventTitle)) {
                    totalPaid += pay.getAmount();
                }
            }
            return totalPaid;
        }

        private void doPay() {
            // Sum category amounts by event (proposal)
            Map<Proposal, Double> amountByProposal = new HashMap<>();

            for (CategoryRow row : rows) {
                if (row.checkBox == null || !row.checkBox.isSelected()) continue;

                double cur = amountByProposal.getOrDefault(row.proposal, 0.0);
                amountByProposal.put(row.proposal, cur + row.amount);
            }

            if (amountByProposal.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please select at least one category.");
                return;
            }

            List<Payment> newPayments = new ArrayList<>();

            for (Map.Entry<Proposal, Double> e : amountByProposal.entrySet()) {
                Proposal p = e.getKey();
                double requestedAmount = e.getValue();

                double required  = p.getAmount();
                double already   = getTotalPaidForProposal(p, student);
                double remaining = required - already;
                if (remaining <= 1e-6) {
                    continue; // this event already fully paid
                }

                double amountToPay = Math.min(requestedAmount, remaining);

                Payment payment = new Payment(
                        student.getFullName(),
                        student.getId(),
                        p.getTitle(),        // event name
                        amountToPay,
                        officer.getFullName()
                );
                LocalDatabase.addPayment(payment);
                newPayments.add(payment);
            }

            if (newPayments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Selected categories are already fully paid.");
                return;
            }

            new ReceiptFrame(newPayments).setVisible(true);
            JOptionPane.showMessageDialog(this,
                    "Partial payment recorded based on selected categories.");
            dispose();
        }
    }

    // ==========================================================
    // Simple text list of all payments (existing feature)
    // ==========================================================
    private void showPayments() {
        StringBuilder sb = new StringBuilder();
        for (Payment p : LocalDatabase.payments) {
            sb.append(String.format("%s | %s | %s | ₱%.2f | %s\n",
                    p.getStudentName(),
                    p.getStudentId(),
                    p.getEventName(),
                    p.getAmount(),
                    p.getOfficerName()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this,
                new JScrollPane(ta),
                "All Payments",
                JOptionPane.INFORMATION_MESSAGE);
    }
}