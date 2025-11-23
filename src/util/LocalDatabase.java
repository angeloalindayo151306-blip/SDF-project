package budgetsystem.util;

import budgetsystem.model.*;

import java.io.*;
import java.util.*;

public class LocalDatabase {

    private static final String STUDENTS_FILE  = "students.txt";
    private static final String OFFICERS_FILE  = "officers.txt";
    private static final String DEAN_FILE      = "dean.txt";
    private static final String PROPOSALS_FILE = "proposals.txt";
    private static final String PAYMENTS_FILE  = "payments.txt";

    public static final List<Student> students = new ArrayList<>();
    public static final List<Officer> officers = new ArrayList<>();
    public static Dean dean = null;
    public static final LinkedList<Proposal> proposals = new LinkedList<>();
    public static final LinkedList<Payment> payments = new LinkedList<>();

    // ==========================================================
    // 🔥 OPTION A: Delete ALL data files every time the program starts
    // ==========================================================
    static {
        deleteAllFiles();  
        loadAll();
    }

    // Delete all saved data files
    private static void deleteAllFiles() {
        deleteFile(STUDENTS_FILE);
        deleteFile(OFFICERS_FILE);
        deleteFile(DEAN_FILE);
        deleteFile(PROPOSALS_FILE);
        deleteFile(PAYMENTS_FILE);
    }

    private static void deleteFile(String name) {
        File f = new File(name);
        if (f.exists()) f.delete();
    }

    // ==========================================================
    // Load (will load empty lists since files are deleted)
    // ==========================================================
    public static void loadAll() {
        students.clear();
        officers.clear();
        proposals.clear();
        payments.clear();
        dean = null;

        loadStudents();
        loadOfficers();
        loadDean();
        loadProposals();
        loadPayments();
    }

    private static void loadStudents() {
        for (String line : readAllLines(STUDENTS_FILE)) {
            String[] f = splitCSV(line);
            if (f[0].equals("Student")) students.add(Student.fromCSV(f));
        }
    }

    private static void loadOfficers() {
        for (String line : readAllLines(OFFICERS_FILE)) {
            String[] f = splitCSV(line);
            if (f[0].equals("Officer")) officers.add(Officer.fromCSV(f));
        }
    }

    private static void loadDean() {
        for (String line : readAllLines(DEAN_FILE)) {
            String[] f = splitCSV(line);
            if (f[0].equals("Dean")) {
                dean = Dean.fromCSV(f);
                break;
            }
        }
    }

    private static void loadProposals() {
        for (String line : readAllLines(PROPOSALS_FILE)) {
            String[] f = splitCSV(line);
            if (f[0].equals("Proposal")) proposals.add(Proposal.fromCSV(f));
        }
    }

    private static void loadPayments() {
        for (String line : readAllLines(PAYMENTS_FILE)) {
            String[] f = splitCSV(line);
            if (f[0].equals("Payment")) payments.add(Payment.fromCSV(f));
        }
    }

    // ==========================================================
    // File reading/writing helpers
    // ==========================================================
    private static List<String> readAllLines(String filename) {
        List<String> out = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return out;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) out.add(line.trim());
        } catch (IOException ignored) {}
        return out;
    }

    private static void appendLine(String filename, String line) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.println(line);
        } catch (IOException ignored) {}
    }

    private static String[] splitCSV(String line) {
        String placeholder = "__COMMA__";
        line = line.replace("&#44;", placeholder);
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace(placeholder, ",");
        }
        return parts;
    }

    // ==========================================================
    // Add & update operations
    // ==========================================================
    public static void addStudent(Student s) {
        students.add(s);
        appendLine(STUDENTS_FILE, s.toCSV());
    }

    public static void addOfficer(Officer o) {
        officers.add(o);
        appendLine(OFFICERS_FILE, o.toCSV());
    }

    public static boolean addDean(Dean d) {
        if (dean != null) return false;
        dean = d;
        appendLine(DEAN_FILE, d.toCSV());
        return true;
    }

    public static void addProposal(Proposal p) {
        proposals.add(p);
        appendLine(PROPOSALS_FILE, p.toCSV());
    }

    public static void updateProposalsFile() {
        List<String> lines = new ArrayList<>();
        for (Proposal p : proposals) lines.add(p.toCSV());
        try (PrintWriter pw = new PrintWriter(new FileWriter(PROPOSALS_FILE, false))) {
            for (String s : lines) pw.println(s);
        } catch (IOException ignored) {}
    }

    public static void addPayment(Payment p) {
        payments.add(p);
        appendLine(PAYMENTS_FILE, p.toCSV());
    }

    public static List<Proposal> getApprovedProposals() {
        List<Proposal> out = new ArrayList<>();
        for (Proposal p : proposals) {
            if ("Approved".equalsIgnoreCase(p.getStatus())) out.add(p);
        }
        return out;
    }
}
