package budgetsystem.model;

public class Officer extends User {

    private String course;
    private String yearLevel;
    private String position;

    public Officer(
            String id,
            String firstName,
            String middleName,
            String lastName,
            String dob,
            int age,
            String username,
            String password,
            String course,
            String yearLevel,
            String position
    ) {
        super(id, firstName, middleName, lastName, dob, age, username, password);
        this.course = course == null ? "" : course;
        this.yearLevel = yearLevel == null ? "" : yearLevel;
        this.position = position == null ? "" : position;
    }

    public String getCourse() { return course; }
    public String getYearLevel() { return yearLevel; }
    public String getPosition() { return position; }

    @Override
    public String toCSV() {
        return String.join(",",
                "Officer",
                escape(id),
                escape(firstName),
                escape(middleName),
                escape(lastName),
                escape(dob),
                String.valueOf(age),
                escape(username),
                escape(password),
                escape(course),
                escape(yearLevel),
                escape(position)
        );
    }

    public static Officer fromCSV(String[] f) {
        // Officer,id,first,middle,last,dob,age,username,password,course,yearLevel,position
        return new Officer(
                unescape(f[1]),
                unescape(f[2]),
                unescape(f[3]),
                unescape(f[4]),
                unescape(f[5]),
                Integer.parseInt(f[6]),
                unescape(f[7]),
                unescape(f[8]),
                unescape(f[9]),
                unescape(f[10]),
                unescape(f[11])
        );
    }

    private static String escape(String s) { return (s == null ? "" : s.replace(",", "&#44;")); }
    private static String unescape(String s) { return (s == null ? "" : s.replace("&#44;", ",")); }
}
