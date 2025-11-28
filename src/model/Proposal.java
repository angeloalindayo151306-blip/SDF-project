package budgetsystem.model;

public class Proposal {

    private String proposalId;
    private String studentId;
    private String title;
    private String description;

    private double amount;            // Total requested amount
    private int numberOfStudents;     // NEW
    private double amountPerStudent;  // NEW (auto computed)

    private String status;  // Pending / Approved / Rejected


    public Proposal(String proposalId,
                    String studentId,
                    String title,
                    String description,
                    double amount,
                    int numberOfStudents,
                    String status) {

        this.proposalId = proposalId;
        this.studentId = studentId;
        this.title = title;
        this.description = description;

        this.amount = amount;
        this.numberOfStudents = numberOfStudents;

        if (numberOfStudents > 0) {
            this.amountPerStudent = amount / numberOfStudents;
        } else {
            this.amountPerStudent = 0;
        }

        this.status = status;
    }


    // Getters
    public String getProposalId() { return proposalId; }
    public String getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public int getNumberOfStudents() { return numberOfStudents; }
    public double getAmountPerStudent() { return amountPerStudent; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }


    // CSV export
    public String toCSV() {
        return String.join(",",
                "Proposal",
                escape(proposalId),
                escape(studentId),
                escape(title),
                escape(description),
                String.valueOf(amount),
                String.valueOf(numberOfStudents),
                String.valueOf(amountPerStudent),
                escape(status)
        );
    }


    // CSV import
    public static Proposal fromCSV(String[] f) {
        return new Proposal(
                unescape(f[1]),
                unescape(f[2]),
                unescape(f[3]),
                unescape(f[4]),
                Double.parseDouble(f[5]),
                Integer.parseInt(f[6]),
                unescape(f[8])
        );
    }


    private static String escape(String s) {
        return (s == null ? "" : s.replace(",", "&#44;"));
    }

    private static String unescape(String s) {
        return (s == null ? "" : s.replace("&#44;", ","));
    }
}
