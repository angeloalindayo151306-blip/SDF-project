package budgetsystem.model;

public class Dean extends User {

    public Dean(
            String firstName,
            String middleName,
            String lastName,
            String dob,
            int age,
            String username,
            String password
    ) {
        super("DEAN", firstName, middleName, lastName, dob, age, username, password);
    }

    @Override
    public String toCSV() {
        return String.join(",",
                "Dean",
                escape(id),          // always "DEAN"
                escape(firstName),
                escape(middleName),
                escape(lastName),
                escape(dob),
                String.valueOf(age),
                escape(username),
                escape(password)
        );
    }

    public static Dean fromCSV(String[] f) {
        // Dean,DEAN,first,middle,last,dob,age,username,password
        return new Dean(
                unescape(f[2]),
                unescape(f[3]),
                unescape(f[4]),
                unescape(f[5]),
                Integer.parseInt(f[6]),
                unescape(f[7]),
                unescape(f[8])
        );
    }

    private static String escape(String s) { return (s == null ? "" : s.replace(",", "&#44;")); }
    private static String unescape(String s) { return (s == null ? "" : s.replace("&#44;", ",")); }
}