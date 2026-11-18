import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Reusable validation rules used both by the console input helper and by the manager classes. */
public final class Validator {
    public static final int MAX_AGE = 120;
    public static final BigDecimal MAX_MONEY = new BigDecimal("10000000");

    private Validator() { }

    public static boolean isValidName(String s) {
        return s != null && s.trim().matches("[A-Za-z][A-Za-z .'-]{1,49}");
    }

    public static String cleanPhone(String s) {
        return s == null ? "" : s.replaceAll("[\\s-]", "");
    }

    public static boolean isValidPhone(String s) {
        return cleanPhone(s).matches("\\+?\\d{10,13}");
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= MAX_AGE;
    }

    /** Free text such as address or medical condition: 3-100 characters. */
    public static boolean isValidText(String s) {
        return s != null && s.trim().length() >= 3 && s.trim().length() <= 100;
    }

    /** Returns "Male", "Female" or "Other"; null if the input is not recognised. */
    public static String normalizeGender(String s) {
        if (s == null) return null;
        switch (s.trim().toLowerCase()) {
            case "m": case "male": return "Male";
            case "f": case "female": return "Female";
            case "o": case "other": return "Other";
            default: return null;
        }
    }

    public static boolean isValidId(String id, String prefix) {
        return id != null && id.matches(prefix + "\\d{3,}");
    }

    public static int idNumber(String id) {
        return Integer.parseInt(id.substring(1));
    }

    public static LocalDate parseDate(String s) throws HospitalException {
        if (s == null || !s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new HospitalException("Invalid date format. Use YYYY-MM-DD (e.g. 2027-03-15).");
        }
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new HospitalException("That date does not exist in the calendar.");
        }
    }

    public static LocalTime parseTime(String s) throws HospitalException {
        if (s == null || !s.matches("\\d{2}:\\d{2}")) {
            throw new HospitalException("Invalid time format. Use 24-hour HH:MM (e.g. 14:30).");
        }
        try {
            return LocalTime.parse(s);
        } catch (DateTimeParseException e) {
            throw new HospitalException("Invalid time. Hours must be 00-23 and minutes 00-59.");
        }
    }

    public static BigDecimal parseMoney(String s) throws HospitalException {
        if (s == null || !s.matches("\\d+(\\.\\d{1,2})?")) {
            throw new HospitalException("Invalid amount. Enter a non-negative number with at most 2 decimals.");
        }
        BigDecimal value = new BigDecimal(s).setScale(2, java.math.RoundingMode.HALF_UP);
        if (value.compareTo(MAX_MONEY) > 0) {
            throw new HospitalException("Amount is too large (maximum is " + MAX_MONEY + ").");
        }
        return value;
    }
}
