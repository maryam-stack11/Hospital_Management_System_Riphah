import java.math.BigDecimal;

/** Small helper class that keeps all console formatting (borders, messages, tables) in one place. */
public final class UI {
    public static final int WIDTH = 64;

    private UI() { }

    public static void line() { System.out.println("=".repeat(WIDTH)); }

    public static void thinLine() { System.out.println("-".repeat(WIDTH + 14)); }

    public static void center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        System.out.println(" ".repeat(pad) + text);
    }

    public static void heading(String title) {
        System.out.println();
        line();
        center(title);
        line();
    }

    public static void banner() {
        System.out.println();
        System.out.println("+" + "=".repeat(WIDTH - 2) + "+");
        System.out.println("|" + " ".repeat(WIDTH - 2) + "|");
        boxLine("   _   _  ___  ____  ____  ___ _____  _    _     ");
        boxLine("  | | | |/ _ \\/ ___||  _ \\|_ _|_   _|/ \\  | |    ");
        boxLine("  | |_| | | | \\___ \\| |_) || |  | | / _ \\ | |    ");
        boxLine("  |  _  | |_| |___) |  __/ | |  | |/ ___ \\| |___ ");
        boxLine("  |_| |_|\\___/|____/|_|   |___| |_/_/   \\_\\_____|");
        System.out.println("|" + " ".repeat(WIDTH - 2) + "|");
        boxLine("HOSPITAL MANAGEMENT SYSTEM", true);
        boxLine("RIPHAH INTERNATIONAL UNIVERSITY", true);
        boxLine("Java Object-Oriented Programming Project", true);
        System.out.println("|" + " ".repeat(WIDTH - 2) + "|");
        System.out.println("+" + "=".repeat(WIDTH - 2) + "+");
    }

    private static void boxLine(String text) { boxLine(text, false); }

    private static void boxLine(String text, boolean centered) {
        int inner = WIDTH - 2;
        String content = text;
        if (centered) {
            int pad = Math.max(0, (inner - text.length()) / 2);
            content = " ".repeat(pad) + text;
        }
        if (content.length() < inner) {
            content = content + " ".repeat(inner - content.length());
        }
        System.out.println("|" + content.substring(0, inner) + "|");
    }

    /** Prints a numbered menu; the last option is conventionally "Return"/"Exit". */
    public static void menu(String title, String... options) {
        heading(title);
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, options[i]);
        }
        line();
    }

    public static void success(String msg) { System.out.println("\n[SUCCESS] " + msg); }

    public static void error(String msg) { System.out.println("\n[ERROR] " + msg); }

    public static void info(String msg) { System.out.println("\n[INFO] " + msg); }

    public static void warn(String msg) { System.out.println("\n[WARNING] " + msg); }

    public static void field(String label, String value) {
        System.out.printf("  %-24s: %s%n", label, value);
    }

    /** Pads or truncates text so table columns line up. */
    public static String fit(String text, int width) {
        if (text == null) text = "";
        if (text.length() > width) return text.substring(0, width - 1) + "~";
        return String.format("%-" + width + "s", text);
    }

    public static String money(BigDecimal amount) {
        return "Rs. " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
