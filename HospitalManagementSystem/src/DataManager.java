import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * File persistence using plain CSV text files (java.nio only, no libraries).
 * Missing files are treated as empty; corrupted lines are skipped instead of crashing.
 */
public class DataManager {

    /** One parser per file type: turns the fields of a line into an object. */
    private interface RecordParser {
        void parse(String[] fields) throws Exception;
    }

    private final Path dir;
    private int skippedRecords = 0;

    public DataManager(String directory) {
        this.dir = Paths.get(directory);
    }

    public int getSkippedRecords() { return skippedRecords; }

    // ---------------------------------------------------------------- load

    public void loadAll(PatientManager pm, DoctorManager dm, AppointmentManager am, BillingManager bm) {
        skippedRecords = 0;
        loadFile("patients.csv", 7, f -> pm.addLoadedPatient(
                new Patient(f[0], f[1], f[4], Integer.parseInt(f[2]), f[3], f[5], f[6])));

        loadFile("doctors.csv", 6, f -> dm.addLoadedDoctor(
                new Doctor(f[0], f[1], f[3], f[2], Validator.parseMoney(f[4]), parseBoolean(f[5]))));

        loadFile("appointments.csv", 6, f -> {
            Patient p = pm.findById(f[1]);
            Doctor d = dm.findById(f[2]);
            if (p == null || d == null) throw new HospitalException("Appointment refers to a missing patient/doctor");
            am.addLoadedAppointment(new Appointment(f[0], p, d, Validator.parseDate(f[3]),
                    Validator.parseTime(f[4]), Appointment.Status.fromLabel(f[5])));
        });

        loadFile("bills.csv", 6, f -> {
            Appointment a = am.findById(f[2]);
            if (a == null || !a.getPatient().getId().equals(f[1])) {
                throw new HospitalException("Bill refers to a missing appointment");
            }
            bm.addLoadedBill(new Billing(f[0], f[1], f[2], Validator.parseMoney(f[3]),
                    Validator.parseMoney(f[4]), Validator.parseMoney(f[5])));
        });
    }

    private static boolean parseBoolean(String s) throws HospitalException {
        if (s.equals("true")) return true;
        if (s.equals("false")) return false;
        throw new HospitalException("Bad boolean: " + s);
    }

    private void loadFile(String fileName, int expectedFields, RecordParser parser) {
        Path file = dir.resolve(fileName);
        if (!Files.exists(file)) return;                       // missing file = no records
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            UI.warn("Could not read " + fileName + ": " + e.getMessage());
            return;
        }
        for (int i = 1; i < lines.size(); i++) {               // line 0 is the header
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            try {
                String[] fields = split(line);
                if (fields.length != expectedFields) throw new HospitalException("Wrong number of fields");
                parser.parse(fields);
            } catch (Exception e) {
                skippedRecords++;
                UI.warn("Skipped corrupted record in " + fileName + " (line " + (i + 1) + "): " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- save

    public boolean saveAll(PatientManager pm, DoctorManager dm, AppointmentManager am, BillingManager bm) {
        try {
            Files.createDirectories(dir);
            List<String> rows = new ArrayList<>();

            rows.add("id,name,age,gender,contact,address,condition");
            for (Patient p : pm.getAllPatients()) {
                rows.add(join(p.getId(), p.getName(), String.valueOf(p.getAge()), p.getGender(),
                        p.getContactNumber(), p.getAddress(), p.getMedicalCondition()));
            }
            write("patients.csv", rows);

            rows = new ArrayList<>();
            rows.add("id,name,specialization,contact,fee,available");
            for (Doctor d : dm.getAllDoctors()) {
                rows.add(join(d.getId(), d.getName(), d.getSpecialization(), d.getContactNumber(),
                        d.getConsultationFee().toPlainString(), String.valueOf(d.isAvailable())));
            }
            write("doctors.csv", rows);

            rows = new ArrayList<>();
            rows.add("id,patientId,doctorId,date,time,status");
            for (Appointment a : am.getAllAppointments()) {
                rows.add(join(a.getId(), a.getPatient().getId(), a.getDoctor().getId(),
                        a.getDate().toString(), a.getTime().toString(), a.getStatus().getLabel()));
            }
            write("appointments.csv", rows);

            rows = new ArrayList<>();
            rows.add("id,patientId,appointmentId,consultationFee,additionalCharges,amountPaid");
            for (Billing b : bm.getAllBills()) {
                rows.add(join(b.getId(), b.getPatientId(), b.getAppointmentId(),
                        b.getConsultationFee().toPlainString(), b.getAdditionalCharges().toPlainString(),
                        b.getAmountPaid().toPlainString()));
            }
            write("bills.csv", rows);
            return true;
        } catch (IOException e) {
            UI.error("Could not save data: " + e.getMessage());
            return false;
        }
    }

    private void write(String fileName, List<String> rows) throws IOException {
        Files.write(dir.resolve(fileName), rows, StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------- CSV helpers
    // A backslash escapes the next character, so commas inside text are safe.

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace(",", "\\,");
    }

    static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    static String[] split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                cur.append(line.charAt(++i));
            } else if (c == ',') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
