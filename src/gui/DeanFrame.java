package budgetsystem.gui;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import budgetsystem.model.*;
import budgetsystem.util.LocalDatabase;
import java.util.UUID;

public class DeanFrame extends JFrame {
    private Dean dean;
    private JTextArea propArea;

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

        propArea = new JTextArea();
        propArea.setEditable(false);
        add(new JScrollPane(propArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton approveBtn = new JButton("Approve Proposal");
        JButton rejectBtn  = new JButton("Reject Proposal");
        JButton viewPayments = new JButton("View Payments Table");
        JButton logout = new JButton("Logout");
        buttons.add(approveBtn); buttons.add(rejectBtn); buttons.add(viewPayments); buttons.add(logout);
        add(buttons, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> changeProposalStatus(true));
        rejectBtn.addActionListener(e -> changeProposalStatus(false));
        viewPayments.addActionListener(e -> showPaymentsTable());
        logout.addActionListener(e -> { new WelcomeFrame().setVisible(true); dispose(); });

        updateProposals();
    }

    private void updateProposals() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Proposal p : LocalDatabase.proposals) {
            sb.append(String.format("[%d] id=%s | title=%s | amount=₱%.2f | status=%s | submitter=%s\n",
                    i++, p.getProposalId(), p.getTitle(), p.getAmount(), p.getStatus(), p.getStudentId()));
        }
        propArea.setText(sb.toString());
    }

    private void changeProposalStatus(boolean approve) {
        String id = JOptionPane.showInputDialog(this, "Enter proposal ID to " + (approve ? "approve" : "reject") + ":");
        if (id == null || id.trim().isEmpty()) return;
        for (Proposal p : LocalDatabase.proposals) {
            if (p.getProposalId().equals(id.trim())) {
                p.setStatus(approve ? "Approved" : "Rejected");
                LocalDatabase.updateProposalsFile();
                updateProposals();
                JOptionPane.showMessageDialog(this, "Proposal updated.");
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Proposal not found (check ID).");
    }

    private void showPaymentsTable() {
        String[] cols = {"Student Name","Student ID","Event","Amount","Officer/Submitter","Date"};
        Object[][] data = new Object[LocalDatabase.payments.size()][6];
        int r=0;
        for (Payment p : LocalDatabase.payments) {
            data[r][0] = p.getStudentName();
            data[r][1] = p.getStudentId();
            data[r][2] = p.getEventName();
            data[r][3] = p.getAmount();
            data[r][4] = p.getOfficerName();
            data[r][5] = p.getDate().toString();
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
