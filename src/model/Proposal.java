package budgetsystem.model;

public class Proposal {

    private String proposalId;
    private String studentId;
    private String title;
    private String description;
    private double amount;
    private String status;  // Pending / Approved / Rejected

    public Proposal(String proposalId, String studentId,
                    String title, String description,
                    double amount, String status) {

        this.proposalId = proposalId;
        this.studentId = studentId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public String getProposalId() { return proposalId; }
    public String getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String toCSV() {
        return String.join(",",
                "Proposal",
                escape(proposalId),
                escape(studentId),
                escape(title),
                escape(description),
                String.valueOf(amount),
                escape(status)
        );
    }

    // Format: Proposal,id,studentId,title,description,amount,status
    public static Proposal fromCSV(String[] f) {
        return new Proposal(
                unescape(f[1]),
                unescape(f[2]),
                unescape(f[3]),
                unescape(f[4]),
                Double.parseDouble(f[5]),
                unescape(f[6])
        );
    }

    private static String escape(String s) {
        return (s == null ? "" : s.replace(",", "&#44;"));
    }

    private static String unescape(String s) {
        return (s == null ? "" : s.replace("&#44;", ","));
    }
}
