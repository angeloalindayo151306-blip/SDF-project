package budgetsystem.model;

import java.time.LocalDate;

public class Proposal {

    private String proposalId;
    private String studentId;       // submitter (officer)
    private String title;           // event name
    private String description;     // includes category breakdown text

    // Per‑student required amount for this event
    private double amount;
    private int numberOfStudents;
    private double amountPerStudent;   // kept for compatibility / reference

    private String status;             // Pending / Approved / Rejected

    // Event timeline
    private LocalDate startDate;       // can be null
    private LocalDate endDate;         // can be null

    // ------------------------------------------------------------
    // Full constructor with dates
    // ------------------------------------------------------------
    public Proposal(String proposalId,
                    String studentId,
                    String title,
                    String description,
                    double amount,
                    int numberOfStudents,
                    String status,
                    LocalDate startDate,
                    LocalDate endDate) {

        this.proposalId = proposalId;
        this.studentId = studentId;
        this.title = title;
        this.description = description;

        this.amount = amount;                    // per-student required
        this.numberOfStudents = numberOfStudents;

        if (numberOfStudents > 0) {
            this.amountPerStudent = amount / numberOfStudents;
        } else {
            this.amountPerStudent = 0;
        }

        this.status = status == null ? "" : status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Old-style constructor (no dates) kept for compatibility
    public Proposal(String proposalId,
                    String studentId,
                    String title,
                    String description,
                    double amount,
                    int numberOfStudents,
                    String status) {
        this(proposalId, studentId, title, description,
             amount, numberOfStudents, status, null, null);
    }

    // ------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------
    public String getProposalId()      { return proposalId; }
    public String getStudentId()       { return studentId; }
    public String getTitle()           { return title; }
    public String getDescription()     { return description; }
    public double getAmount()          { return amount; }           // per-student
    public int getNumberOfStudents()   { return numberOfStudents; }
    public double getAmountPerStudent(){ return amountPerStudent; }
    public String getStatus()          { return status; }
    public LocalDate getStartDate()    { return startDate; }
    public LocalDate getEndDate()      { return endDate; }

    public void setStatus(String status)       { this.status = status; }
    public void setStartDate(LocalDate d)      { this.startDate = d; }
    public void setEndDate(LocalDate d)        { this.endDate = d; }

    // Total budget allocation for this event:
    // per-student amount * number of students
    public double getBudgetAllocation() {
        return amount * numberOfStudents;
    }

    // ------------------------------------------------------------
    // Helper methods for "ongoing" / "done" based on timeline
    // ------------------------------------------------------------
    public boolean isOngoing(LocalDate today) {
        if (today == null) today = LocalDate.now();

        LocalDate s = (startDate != null) ? startDate : LocalDate.MIN;
        LocalDate e = (endDate != null) ? endDate   : LocalDate.MAX;

        return !today.isBefore(s) && !today.isAfter(e);
    }

    public boolean isDone(LocalDate today) {
        if (today == null) today = LocalDate.now();

        LocalDate e = (endDate != null) ? endDate : startDate;
        if (e == null) return false;

        return today.isAfter(e);
    }

    // ------------------------------------------------------------
    // CSV export: type, id, studentId, title, desc, amount,
    //             numStudents, amountPerStudent, status, startDate, endDate
    // ------------------------------------------------------------
    public String toCSV() {
        String sd = (startDate == null ? "" : startDate.toString());
        String ed = (endDate   == null ? "" : endDate.toString());

        return String.join(",",
                "Proposal",
                escape(proposalId),
                escape(studentId),
                escape(title),
                escape(description),
                String.valueOf(amount),
                String.valueOf(numberOfStudents),
                String.valueOf(amountPerStudent),
                escape(status),
                sd,
                ed
        );
    }

    // ------------------------------------------------------------
    // CSV import (handles old lines without dates as well)
    // ------------------------------------------------------------
    public static Proposal fromCSV(String[] f) {
        // old format: 0..8; new format: 0..10 with start/end date at 9/10
        LocalDate startDate = null;
        LocalDate endDate   = null;

        if (f.length > 9 && f[9] != null && !f[9].isEmpty()) {
            startDate = LocalDate.parse(f[9]);
        }
        if (f.length > 10 && f[10] != null && !f[10].isEmpty()) {
            endDate = LocalDate.parse(f[10]);
        }

        return new Proposal(
                unescape(f[1]),
                unescape(f[2]),
                unescape(f[3]),
                unescape(f[4]),
                Double.parseDouble(f[5]),
                Integer.parseInt(f[6]),
                unescape(f[8]),
                startDate,
                endDate
        );
    }

    // ------------------------------------------------------------
    // CSV helpers
    // ------------------------------------------------------------
    private static String escape(String s) {
        return (s == null ? "" : s.replace(",", "&#44;"));
    }

    private static String unescape(String s) {
        return (s == null ? "" : s.replace("&#44;", ","));
    }
}