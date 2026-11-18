/**
 * System Control module: owns the four managers, shows the welcome screen and main menu,
 * and coordinates saving/loading. This is the "Module 1" of the proposal.
 */
public class HospitalManagementSystem {
    private final InputHelper input;
    private final DataManager dataManager;
    private final PatientManager patientManager;
    private final DoctorManager doctorManager;
    private final AppointmentManager appointmentManager;
    private final BillingManager billingManager;

    public HospitalManagementSystem(InputHelper input, String dataDirectory) {
        this.input = input;
        this.dataManager = new DataManager(dataDirectory);
        Runnable saver = this::save;   // every manager calls this after a change

        patientManager = new PatientManager(input, saver);
        doctorManager = new DoctorManager(input, saver);
        appointmentManager = new AppointmentManager(patientManager, doctorManager, input, saver);
        billingManager = new BillingManager(appointmentManager, patientManager, input, saver);
        patientManager.setAppointmentManager(appointmentManager);
        doctorManager.setAppointmentManager(appointmentManager);

        dataManager.loadAll(patientManager, doctorManager, appointmentManager, billingManager);
    }

    public PatientManager getPatientManager() { return patientManager; }
    public DoctorManager getDoctorManager() { return doctorManager; }
    public AppointmentManager getAppointmentManager() { return appointmentManager; }
    public BillingManager getBillingManager() { return billingManager; }
    public void saveData() { save(); }

    private void save() {
        dataManager.saveAll(patientManager, doctorManager, appointmentManager, billingManager);
    }

    public void start() {
        UI.banner();
        System.out.println("Loaded: " + patientManager.getPatientCount() + " patient(s), "
                + doctorManager.getDoctorCount() + " doctor(s), " + appointmentManager.getCount()
                + " appointment(s), " + billingManager.getCount() + " bill(s).");
        try {
            boolean running = true;
            while (running) {
                UI.menu("MAIN MENU", "Patient Management", "Doctor Management", "Appointment Management",
                        "Billing Management", "View Hospital Statistics", "Exit System");
                switch (input.readChoice(1, 6)) {
                    case 1 -> patientManager.runMenu();
                    case 2 -> doctorManager.runMenu();
                    case 3 -> appointmentManager.runMenu();
                    case 4 -> billingManager.runMenu();
                    case 5 -> showStatistics();
                    default -> running = false;
                }
            }
        } catch (InputHelper.InputClosedException e) {
            UI.warn("Input ended unexpectedly. Saving data and exiting.");
        }
        save();
        UI.heading("THANK YOU FOR USING THE SYSTEM");
        System.out.println("All data has been saved. Goodbye!");
    }

    /** Every figure is calculated from the live records - nothing is hardcoded. */
    private void showStatistics() {
        UI.heading("HOSPITAL STATISTICS");
        UI.field("Total patients", String.valueOf(patientManager.getPatientCount()));
        UI.field("Total doctors", String.valueOf(doctorManager.getDoctorCount()));
        UI.field("Total appointments", String.valueOf(appointmentManager.getCount()));
        UI.field("  Scheduled", String.valueOf(appointmentManager.countByStatus(Appointment.Status.SCHEDULED)));
        UI.field("  Completed", String.valueOf(appointmentManager.countByStatus(Appointment.Status.COMPLETED)));
        UI.field("  Cancelled", String.valueOf(appointmentManager.countByStatus(Appointment.Status.CANCELLED)));
        UI.field("Total bills generated", String.valueOf(billingManager.getCount()));
        UI.field("Revenue (paid bills)", UI.money(billingManager.getRevenueFromPaidBills()));
        UI.field("Partial payments held", UI.money(billingManager.getPartialPaymentsReceived()));
        UI.field("Outstanding balance", UI.money(billingManager.getOutstandingBalance()));
        UI.line();
    }
}
