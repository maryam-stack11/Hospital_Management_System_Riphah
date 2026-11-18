import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Manages Billing objects (ArrayList) and the Billing menu. */
public class BillingManager {
    private final ArrayList<Billing> bills = new ArrayList<>();
    private final AppointmentManager appointments;
    private final PatientManager patients;
    private final InputHelper input;
    private final Runnable onChange;

    public BillingManager(AppointmentManager appointments, PatientManager patients,
                          InputHelper input, Runnable onChange) {
        this.appointments = appointments;
        this.patients = patients;
        this.input = input;
        this.onChange = onChange;
    }

    // ---------------------------------------------------------------- logic

    public List<Billing> getAllBills() { return Collections.unmodifiableList(bills); }

    public int getCount() { return bills.size(); }

    private String nextId() {
        int max = 0;
        for (Billing b : bills) max = Math.max(max, Validator.idNumber(b.getId()));
        return String.format("B%03d", max + 1);
    }

    public Billing findById(String id) {
        for (Billing b : bills) {
            if (b.getId().equalsIgnoreCase(id)) return b;
        }
        return null;
    }

    public Billing findByAppointment(String appointmentId) {
        for (Billing b : bills) {
            if (b.getAppointmentId().equalsIgnoreCase(appointmentId)) return b;
        }
        return null;
    }

    public ArrayList<Billing> findByPatient(String patientId) {
        ArrayList<Billing> list = new ArrayList<>();
        for (Billing b : bills) {
            if (b.getPatientId().equalsIgnoreCase(patientId)) list.add(b);
        }
        return list;
    }

    /** Fee is taken automatically from the doctor of the appointment. */
    public Billing generateBill(String appointmentId, BigDecimal additionalCharges) throws HospitalException {
        Appointment appt = appointments.findById(appointmentId);
        if (appt == null) throw new HospitalException("No appointment found with ID " + appointmentId + ".");
        checkBillable(appt);
        if (additionalCharges == null || additionalCharges.signum() < 0) {
            throw new HospitalException("Additional charges cannot be negative.");
        }
        Billing bill = new Billing(nextId(), appt.getPatient().getId(), appt.getId(),
                appt.getDoctor().getConsultationFee(),
                additionalCharges.setScale(2, java.math.RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2));
        bills.add(bill);
        onChange.run();
        return bill;
    }

    /** Rules: appointment must be Completed and must not be billed already. */
    public void checkBillable(Appointment appt) throws HospitalException {
        if (appt.getStatus() != Appointment.Status.COMPLETED) {
            throw new HospitalException("Bills can only be generated for Completed appointments (this one is "
                    + appt.getStatus().getLabel() + ").");
        }
        if (findByAppointment(appt.getId()) != null) {
            throw new HospitalException("A bill already exists for appointment " + appt.getId() + ".");
        }
    }

    public void addLoadedBill(Billing b) throws HospitalException {
        if (!Validator.isValidId(b.getId(), "B")) throw new HospitalException("Bad bill ID: " + b.getId());
        if (findById(b.getId()) != null) throw new HospitalException("Duplicate bill ID: " + b.getId());
        if (findByAppointment(b.getAppointmentId()) != null) throw new HospitalException("Duplicate bill for appointment");
        if (b.getAmountPaid().signum() < 0 || b.getAmountPaid().compareTo(b.getTotalAmount()) > 0) {
            throw new HospitalException("Invalid amount paid in bill " + b.getId());
        }
        bills.add(b);
    }

    public void markPaid(String billId) throws HospitalException {
        requireBill(billId).markPaid();
        onChange.run();
    }

    public void markUnpaid(String billId) throws HospitalException {
        requireBill(billId).markUnpaid();
        onChange.run();
    }

    public void markPartiallyPaid(String billId, BigDecimal totalPaidSoFar) throws HospitalException {
        requireBill(billId).recordPartialPayment(totalPaidSoFar);
        onChange.run();
    }

    public void updateAdditionalCharges(String billId, BigDecimal charges) throws HospitalException {
        requireBill(billId).updateAdditionalCharges(charges);
        onChange.run();
    }

    public void deleteUnpaidBill(String billId) throws HospitalException {
        Billing bill = requireBill(billId);
        if (bill.getAmountPaid().signum() != 0) {
            throw new HospitalException("A bill with recorded payments cannot be deleted.");
        }
        bills.remove(bill);
        onChange.run();
    }

    private Billing requireBill(String id) throws HospitalException {
        Billing b = findById(id);
        if (b == null) throw new HospitalException("No bill found with ID " + id + ".");
        return b;
    }

    /** Revenue = amount collected on bills that are fully paid. */
    public BigDecimal getRevenueFromPaidBills() {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Billing b : bills) {
            if (b.getPaymentStatus() == Billing.PaymentStatus.PAID) sum = sum.add(b.getAmountPaid());
        }
        return sum;
    }

    /** Money received on bills that are only partially paid. */
    public BigDecimal getPartialPaymentsReceived() {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Billing b : bills) {
            if (b.getPaymentStatus() == Billing.PaymentStatus.PARTIALLY_PAID) sum = sum.add(b.getAmountPaid());
        }
        return sum;
    }

    public BigDecimal getOutstandingBalance() {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Billing b : bills) sum = sum.add(b.getBalance());
        return sum;
    }

    // ----------------------------------------------------------------- menu

    public void runMenu() {
        while (true) {
            UI.menu("BILLING MANAGEMENT", "Generate Bill", "View All Bills", "Search Bill by ID",
                    "View Patient Billing History", "Update Payment Status", "Return to Main Menu");
            int choice = input.readChoice(1, 6);
            try {
                switch (choice) {
                    case 1 -> generateUI();
                    case 2 -> viewAllUI();
                    case 3 -> searchUI();
                    case 4 -> historyUI();
                    case 5 -> updatePaymentUI();
                    default -> { return; }
                }
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    private void printTable(List<Billing> list) {
        System.out.println(Billing.tableHeader());
        UI.thinLine();
        for (Billing b : list) System.out.println(b.toTableRow());
        UI.thinLine();
        System.out.println("Total: " + list.size() + " bill(s)");
    }

    private void generateUI() throws HospitalException {
        UI.heading("GENERATE BILL");
        Appointment appt = appointments.findById(input.readId("Appointment ID"));
        if (appt == null) throw new HospitalException("Appointment not found.");
        checkBillable(appt);   // fail early, before asking for charges
        appt.displayDetails();
        UI.field("Consultation Fee", UI.money(appt.getDoctor().getConsultationFee()) + " (from doctor record)");
        BigDecimal extra = input.readMoney("Additional charges (Rs., 0 for none)", true, null);
        Billing b = generateBill(appt.getId(), extra);
        UI.success("Bill " + b.getId() + " generated. Total: " + UI.money(b.getTotalAmount()));
        b.displayDetails();
    }

    private void viewAllUI() {
        UI.heading("ALL BILLS");
        if (bills.isEmpty()) UI.info("No bills generated yet.");
        else printTable(bills);
    }

    private void searchUI() {
        UI.heading("SEARCH BILL BY ID");
        String id = input.readId("Enter bill ID (e.g. B001)");
        Billing b = findById(id);
        if (b == null) UI.error("No bill found with ID " + id + ".");
        else b.displayDetails();
    }

    private void historyUI() {
        UI.heading("PATIENT BILLING HISTORY");
        String pid = input.readId("Patient ID");
        Patient p = patients.findById(pid);
        if (p == null) {
            UI.error("No patient found with ID " + pid + ".");
            return;
        }
        ArrayList<Billing> list = findByPatient(pid);
        System.out.println("Patient: " + p.getId() + " - " + p.getName());
        if (list.isEmpty()) {
            UI.info("This patient has no bills.");
            return;
        }
        printTable(list);
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        BigDecimal due = BigDecimal.ZERO.setScale(2);
        for (Billing b : list) {
            total = total.add(b.getTotalAmount());
            due = due.add(b.getBalance());
        }
        UI.field("Total billed", UI.money(total));
        UI.field("Outstanding", UI.money(due));
    }

    private void updatePaymentUI() throws HospitalException {
        UI.heading("UPDATE PAYMENT STATUS");
        Billing b = requireBill(input.readId("Bill ID"));
        b.displayDetails();
        System.out.println("  [1] Paid\n  [2] Unpaid\n  [3] Partially Paid\n  [4] Back");
        int c = input.readChoice(1, 4);
        switch (c) {
            case 1 -> markPaid(b.getId());
            case 2 -> markUnpaid(b.getId());
            case 3 -> {
                BigDecimal amount = input.readMoney("Total amount paid so far (Rs.)", false, null);
                markPartiallyPaid(b.getId(), amount);
            }
            default -> { return; }
        }
        UI.success("Payment status updated to " + b.getPaymentStatus().getLabel() + ".");
        b.displayDetails();
    }
}
