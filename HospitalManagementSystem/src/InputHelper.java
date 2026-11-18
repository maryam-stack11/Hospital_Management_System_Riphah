import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Wraps Scanner so every menu reads and validates input in the same way.
 * Invalid input never crashes the program: the user is simply asked again.
 */
public class InputHelper {

    /** Thrown when the input stream ends (e.g. Ctrl+D or a finished test script). */
    public static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public InputClosedException() { super("Input stream closed"); }
    }

    private final Scanner scanner;

    public InputHelper(InputStream in) {
        this.scanner = new Scanner(in);
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) throw new InputClosedException();
        return scanner.nextLine().trim();
    }

    public int readChoice(int min, int max) {
        while (true) {
            String s = readLine("Enter your choice: ");
            try {
                int c = Integer.parseInt(s);
                if (c >= min && c <= max) return c;
            } catch (NumberFormatException ignored) {
                // fall through to the error message below
            }
            UI.error("Invalid choice. Please enter a number between " + min + " and " + max + ".");
        }
    }

    /** Core loop: keeps asking until the predicate accepts. If 'current' is given, Enter keeps it. */
    private String readValidated(String label, Predicate<String> ok, String err, String current) {
        String prompt = (current == null) ? label + ": " : label + " [" + current + "] (Enter to keep): ";
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty() && current != null) return current;
            if (ok.test(s)) return s;
            UI.error(err);
        }
    }

    public String readId(String label) {
        return readLine(label + ": ").toUpperCase();
    }

    public String readName(String label, String current) {
        return readValidated(label, Validator::isValidName,
                "Name must be 2-50 characters: letters, spaces, . ' - only.", current);
    }

    public String readPhone(String current) {
        String s = readValidated("Contact number", Validator::isValidPhone,
                "Enter 10-13 digits with an optional leading '+' (e.g. 03001234567).", current);
        return Validator.cleanPhone(s);
    }

    public int readAge(String current) {
        String s = readValidated("Age", x -> x.matches("\\d{1,3}") && Validator.isValidAge(Integer.parseInt(x)),
                "Age must be a whole number between 0 and " + Validator.MAX_AGE + ".", current);
        return Integer.parseInt(s);
    }

    public String readGender(String current) {
        String s = readValidated("Gender (Male/Female/Other)", x -> Validator.normalizeGender(x) != null,
                "Enter Male, Female or Other (M/F/O).", current);
        return Validator.normalizeGender(s);
    }

    public String readText(String label, String current) {
        return readValidated(label, Validator::isValidText, label + " must be 3-100 characters.", current);
    }

    public LocalDate readDate(String label) {
        while (true) {
            try {
                return Validator.parseDate(readLine(label + " (YYYY-MM-DD): "));
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    public LocalTime readTime(String label) {
        while (true) {
            try {
                return Validator.parseTime(readLine(label + " (HH:MM, 24-hour): "));
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    /** Reads a monetary amount. If 'current' is given, Enter keeps it. */
    public BigDecimal readMoney(String label, boolean allowZero, String current) {
        String prompt = (current == null) ? label + ": " : label + " [" + current + "] (Enter to keep): ";
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty() && current != null) return new BigDecimal(current);
            try {
                BigDecimal value = Validator.parseMoney(s);
                if (!allowZero && value.signum() == 0) {
                    UI.error("Amount must be greater than zero.");
                    continue;
                }
                return value;
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    public boolean confirm(String question) {
        while (true) {
            String s = readLine(question + " (y/n): ").toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            UI.error("Please answer y or n.");
        }
    }
}
