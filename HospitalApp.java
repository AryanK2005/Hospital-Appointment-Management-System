import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

/*
 ==========================================================
 HOSPITAL APPOINTMENT MANAGEMENT SYSTEM
 ----------------------------------------------------------
 Author  : Student Project
 Purpose : Console-based Java application demonstrating
           OOP, Collections, Multithreading, Exceptions,
           and File Handling.
 ==========================================================
*/

// ==================== PATIENT CLASS ====================
/*
 Represents a patient entity.
 Demonstrates encapsulation using private fields
 and public getter methods.
*/
class Patient {
    private int id;
    private String name;
    private int age;

    // Constructor initializes patient details
    public Patient(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    // Getter methods (no setters → data protection)
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

// ==================== DOCTOR CLASS ====================
/*
 Represents a doctor entity.
 Stores specialization information for realism.
*/
class Doctor {
    private int id;
    private String name;
    private String specialization;

    public Doctor(int id, String name, String specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

// ==================== APPOINTMENT CLASS ====================
/*
 Links Patient and Doctor together.
 Uses priority to handle emergency appointments.
*/
class Appointment {
    private Patient patient;
    private Doctor doctor;
    private String date;
    private int priority; // 2 = Emergency, 1 = Normal

    public Appointment(Patient patient, Doctor doctor, String date, int priority) {
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    // Provides readable output for display and file storage
    @Override
    public String toString() {
        return "Patient: " + patient.getName()
                + ", Doctor: " + doctor.getName()
                + ", Date: " + date
                + ", Priority: " + (priority == 2 ? "Emergency" : "Normal");
    }
}

// ==================== CUSTOM EXCEPTION ====================
/*
 Custom checked exception for appointment-related issues.
 Improves clarity over generic Exception.
*/
class SlotUnavailableException extends Exception {
    public SlotUnavailableException(String message) {
        super(message);
    }
}

// ==================== REMINDER THREAD ====================
/*
 Demonstrates multithreading.
 Runs independently and reminds user after delay.
*/
class ReminderThread extends Thread {

    @Override
    public void run() {
        try {
            Thread.sleep(3000); // Simulates background task
            System.out.println("\n🔔 Reminder: Review today's appointments!");
        } catch (InterruptedException e) {
            System.out.println("Reminder thread interrupted.");
        }
    }
}

// ==================== MAIN APPLICATION ====================
public class HospitalApp {

    // Single Scanner instance (avoids resource leak)
    private static final Scanner sc = new Scanner(System.in);

    // Dynamic storage using ArrayList
    private static final List<Patient> patients = new ArrayList<>();
    private static final List<Doctor> doctors = new ArrayList<>();

    /*
     PriorityQueue ensures:
     - Emergency appointments are handled first
     - Automatic sorting based on priority value
    */
    private static final PriorityQueue<Appointment> appointments =
            new PriorityQueue<>((a, b) -> b.getPriority() - a.getPriority());

    // ==================== MAIN METHOD ====================
    public static void main(String[] args) {

        // Start background reminder thread
        new ReminderThread().start();

        // Infinite loop keeps application running
        while (true) {
            showMenu();
            int choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1 -> registerPatient();
                    case 2 -> registerDoctor();
                    case 3 -> bookAppointment();
                    case 4 -> viewAppointments();
                    case 5 -> saveToFile();
                    case 6 -> exitApp();
                    default -> System.out.println("❌ Invalid choice!");
                }
            } catch (Exception e) {
                // Centralized error handling
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ==================== MENU ====================
    private static void showMenu() {
        System.out.println("\n===== HOSPITAL APPOINTMENT SYSTEM =====");
        System.out.println("1. Register Patient");
        System.out.println("2. Register Doctor");
        System.out.println("3. Book Appointment");
        System.out.println("4. View Appointments");
        System.out.println("5. Save Appointments to File");
        System.out.println("6. Exit");
        System.out.print("Enter choice: ");
    }

    // ==================== PATIENT REGISTRATION ====================
    private static void registerPatient() {
        System.out.print("Enter Patient ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        // Prevent duplicate patient IDs
        if (findPatientById(id) != null) {
            System.out.println("❌ Patient ID already exists!");
            return;
        }

        System.out.print("Enter Patient Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Age: ");
        int age = sc.nextInt();

        patients.add(new Patient(id, name, age));
        System.out.println("✅ Patient registered successfully.");
    }

    // ==================== DOCTOR REGISTRATION ====================
    private static void registerDoctor() {
        System.out.print("Enter Doctor ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (findDoctorById(id) != null) {
            System.out.println("❌ Doctor ID already exists!");
            return;
        }

        System.out.print("Enter Doctor Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Specialization: ");
        String specialization = sc.nextLine();

        doctors.add(new Doctor(id, name, specialization));
        System.out.println("✅ Doctor registered successfully.");
    }

    // ==================== BOOK APPOINTMENT ====================
    private static void bookAppointment() throws SlotUnavailableException {

        if (patients.isEmpty() || doctors.isEmpty()) {
            throw new SlotUnavailableException("Please register patients and doctors first.");
        }

        System.out.print("Enter Patient ID: ");
        int pid = sc.nextInt();

        System.out.print("Enter Doctor ID: ");
        int did = sc.nextInt();
        sc.nextLine();

        Patient patient = findPatientById(pid);
        Doctor doctor = findDoctorById(did);

        if (patient == null || doctor == null) {
            throw new SlotUnavailableException("Invalid Patient or Doctor ID.");
        }

        System.out.print("Enter Date (dd-mm-yyyy): ");
        String date = sc.nextLine();

        System.out.print("Is this an emergency? (yes/no): ");
        String emergency = sc.nextLine();

        int priority = emergency.equalsIgnoreCase("yes") ? 2 : 1;

        appointments.add(new Appointment(patient, doctor, date, priority));
        System.out.println("✅ Appointment booked successfully.");
    }

    // ==================== VIEW APPOINTMENTS ====================
    private static void viewAppointments() {
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }

        System.out.println("\n--- Appointment List (Priority Order) ---");
        appointments.forEach(System.out::println);
    }

    // ==================== SAVE TO FILE ====================
    private static void saveToFile() throws IOException {

        // Try-with-resources automatically closes FileWriter
        try (FileWriter writer = new FileWriter("appointments.txt", true)) {
            for (Appointment a : appointments) {
                writer.write(a + System.lineSeparator());
            }
        }

        System.out.println("📁 Appointments saved successfully.");
    }

    // ==================== HELPER METHODS ====================
    private static Patient findPatientById(int id) {
        return patients.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private static Doctor findDoctorById(int id) {
        return doctors.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
    }

    // ==================== EXIT ====================
    private static void exitApp() {
        System.out.println("Exiting system...");
        sc.close();
        System.exit(0);
    }
}