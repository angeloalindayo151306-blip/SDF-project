package budgetsystem.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Payment {
    private String studentName;
    private String studentId;
    private String eventName;
    private double amount;
    private String officerName;
    private LocalDate date;

    public Payment(String studentName, String studentId, String eventName,
                   double amount, String officerName) {
        this.studentName = studentName == null ? "" : studentName;
        this.studentId = studentId == null ? "" : studentId;
        this.eventName = eventName == null ? "" : eventName;
        this.amount = amount;
        this.officerName = officerName == null ? "" : officerName;
        this.date = LocalDate.now();
    }

    public String getStudentName() { return studentName; }
    public String getStudentId() { return studentId; }
    public String getEventName() { return eventName; }
    public double getAmount() { return amount; }
    public String getOfficerName() { return officerName; }
    public LocalDate getDate() { return date; }

    // CSV: Payment,studentName,studentId,event,amount,officer,date
    public String toCSV() {
        return String.join(",",
                "Payment",
                escape(studentName),
                escape(studentId),
                escape(eventName),
                String.valueOf(amount),
                escape(officerName),
                date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
    }

    public static Payment fromCSV(String[] f) {
        Payment p = new Payment(unescape(f[1]), unescape(f[2]), unescape(f[3]),
                Double.parseDouble(f[4]), unescape(f[5]));
        p.date = LocalDate.parse(f[6]);
        return p;
    }

    private static String escape(String s) { return s == null ? "" : s.replace(",", "&#44;"); }
    private static String unescape(String s) { return s == null ? "" : s.replace("&#44;", ","); }
}