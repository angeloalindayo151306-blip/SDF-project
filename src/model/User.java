package budgetsystem.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String firstName;
    protected String middleName;
    protected String lastName;
    protected String dob; // yyyy-MM-dd
    protected int age;
    protected String username;
    protected String password;

    public User(String id, String firstName, String middleName, String lastName,
                String dob, int age, String username, String password) {
        this.id = id == null ? "" : id;
        this.firstName = firstName == null ? "" : firstName;
        this.middleName = middleName == null ? "" : middleName;
        this.lastName = lastName == null ? "" : lastName;
        this.dob = dob == null ? "" : dob;
        this.age = age;
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getDob() { return dob; }
    public int getAge() { return age; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public String getFullName() {
        String m = (middleName == null || middleName.isEmpty()) ? "" : (" " + middleName);
        return firstName + m + " " + lastName;
    }

    // subclasses should include role-specific fields in CSV
    public abstract String toCSV();
}
