package budgetsystem.model;

public class Student extends User {

    private String course;
    private String yearLevel;

    public Student(
            String id,
            String firstName,
            String middleName,
            String lastName,
            String dob,       // stored as yyyy-MM-dd
            int age,
            String username,
            String password,
            String course,
            String yearLevel
    ) {
        super(id, firstName, middleName, lastName, dob, age, username, password);
        this.course = course == null ? "" : course;
        this.yearLevel = yearLevel == null ? "" : yearLevel;
    }

    public String getCourse() {
        return course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    @Override
    public String toCSV() {
        return String.join(",",
                "Student",
                escape(id),
                escape(firstName),
                escape(middleName),
                escape(lastName),
                escape(dob),
                String.valueOf(age),
                escape(username),
                escape(password),
                escape(course),
                escape(yearLevel)
        );
    }

    public static Student fromCSV(String[] f) {
        // Student,id,first,middle,last,dob,age,username,password,course,year
        return new Student(
                unescape(f[1]),
                unescape(f[2]),
                unescape(f[3]),
                unescape(f[4]),
                unescape(f[5]),                   // yyyy-MM-dd stored format
                Integer.parseInt(f[6]),
                unescape(f[7]),
                unescape(f[8]),
                unescape(f[9]),
                unescape(f[10])
        );
    }

    // For combo boxes etc.
    @Override
    public String toString() {
        return getId() + " - " + getFullName();
    }

    private static String escape(String s) {
        return (s == null ? "" : s.replace(",", "&#44;"));
    }

    private static String unescape(String s) {
        return (s == null ? "" : s.replace("&#44;", ","));
    }
}