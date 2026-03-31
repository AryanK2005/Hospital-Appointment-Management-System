import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

/*
 =====================================================
 HOSPITAL APPOINTMENT MANAGEMENT SYSTEM
 -----------------------------------------------------
 Features:
 - Patient & Doctor Registration
 - Priority-based Appointment Booking
 - Emergency handling using PriorityQueue
 - File handling for saving appointments
 - Multithreading for reminders
 =====================================================
*/

// -------------------- PATIENT CLASS --------------------
class Patient {
    private int id;
    private String name;
    private int age;

    // Constructor
    public Patient(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    // Getter methods (Encapsulation)
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

// -------------------- DOCTOR CLASS --------------------
class Doctor {
    private int id;
    private String name;
    private String specialization;

    // Constructor
    public Doctor(int id, String name, String specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

// -------------------- APPOINTMENT CLASS --------------------
class Appointment {
    protected Patient patient;
    protected Doctor doctor;
    protected String date;
    protected int priority; // 1 = Normal, 2 = Emergency

    public Appointment(Patient patient, Doctor doctor, String date, int priority) {
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    // Overriding toString() for meaningful display & file writing
    @Override
    public String toString() {
        return "Patient: " + patient.getName()
                + ", Doctor: " + doctor.getName()
                + ", Date: " + date
                + ", Priority: " + (priority == 2 ? "Emergency" : "Normal");
    }
}

// -------------------- CUSTOM EXCEPTION --------------------
class SlotUnavailableException extends Exception {
    private static final long serialVersionUID = 1L;

    public SlotUnavailableException(String message) {
        super(message);
    }
}

// -------------------- REMINDER THREAD --------------------
class ReminderThread extends Thread {

    // Thread runs independently to remind user
    @Override
    public void run() {
        try {
            Thread.sleep(3000); // Delay for reminder
            System.out.println("\n🔔 Reminder: Check today's appointments!");
        } catch (InterruptedException e) {
            System.out.println("Reminder interrupted");
        }
    }
}

// -------------------- MAIN APPLICATION --------------------
public class HospitalApp {

    // Single Scanner object used throughout program
    private static final Scanner sc = new Scanner(System.in);

    // Data storage using Collections
    private static final List<Patient> patients = new ArrayList<>();
    private static final List<Doctor> doctors = new ArrayList<>();

    // PriorityQueue ensures emergency appointments come first
    private static final PriorityQueue<Appointment> appointments =
            new PriorityQueue<>((a, b) -> b.getPriority() - a.getPriority());

    // -------------------- MAIN METHOD --------------------
    public static void main(String[] args) {

        // Start reminder thread
        new ReminderThread().start();

        while (true) {
            System.out.println("\n===== HOSPITAL APPOINTMENT SYSTEM =====");
            System.out.println("1. Register Patient");
            System.out.println("2. Register Doctor");
            System.out.println("3. Book Appointment");
            System.out.println("4. View Appointments");
            System.out.println("5. Save Appointments to File");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1 -> registerPatient();
                    case 2 -> registerDoctor();
                    case 3 -> bookAppointment();
                    case 4 -> viewAppointments();
                    case 5 -> saveToFile();
                    case 6 -> {
                        System.out.println("Exiting system...");
                        sc.close();
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------- REGISTER PATIENT --------------------
    private static void registerPatient() {
        System.out.print("Enter Patient ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Patient Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Age: ");
        int age = sc.nextInt();

        patients.add(new Patient(id, name, age));
        System.out.println("✅ Patient Registered Successfully");
    }

    // -------------------- REGISTER DOCTOR --------------------
    private static void registerDoctor() {
        System.out.print("Enter Doctor ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Doctor Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Specialization: ");
        String specialization = sc.nextLine();

        doctors.add(new Doctor(id, name, specialization));
        System.out.println("✅ Doctor Registered Successfully");
    }

    // -------------------- BOOK APPOINTMENT --------------------
    private static void bookAppointment() throws SlotUnavailableException {

        // Validation check
        if (patients.isEmpty() || doctors.isEmpty()) {
            throw new SlotUnavailableException("Register patient and doctor first!");
        }

        System.out.print("Enter Patient ID: ");
        int pid = sc.nextInt();

        System.out.print("Enter Doctor ID: ");
        int did = sc.nextInt();
        sc.nextLine();

        // Find patient
        Patient patient = patients.stream()
                .filter(p -> p.getId() == pid)
                .findFirst()
                .orElse(null);

        // Find doctor
        Doctor doctor = doctors.stream()
                .filter(d -> d.getId() == did)
                .findFirst()
                .orElse(null);

        if (patient == null || doctor == null) {
            throw new SlotUnavailableException("Invalid patient or doctor ID!");
        }

        System.out.print("Enter Date (dd-mm-yyyy): ");
        String date = sc.nextLine();

        System.out.print("Is this an emergency? (yes/no): ");
        String emergency = sc.nextLine();

        int priority = emergency.equalsIgnoreCase("yes") ? 2 : 1;

        appointments.add(new Appointment(patient, doctor, date, priority));
        System.out.println("✅ Appointment Booked Successfully");
    }

    // -------------------- VIEW APPOINTMENTS --------------------
    private static void viewAppointments() {
        if (appointments.isEmpty()) {
            System.out.println("No appointments available");
            return;
        }

        System.out.println("\n--- Appointments List (Emergency First) ---");
        appointments.forEach(System.out::println);
    }

    // -------------------- SAVE TO FILE --------------------
    private static void saveToFile() throws IOException {

        // Try-with-resources ensures file is properly closed
        try (FileWriter writer = new FileWriter("appointments.txt", true)) {
            for (Appointment a : appointments) {
                writer.write(a + System.lineSeparator());
            }
        }

        System.out.println("📁 Appointments saved to file successfully");
    }
}