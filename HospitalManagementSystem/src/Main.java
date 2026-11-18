/** Entry point. Optional argument: data folder (default "data"). */
public class Main {
    public static void main(String[] args) {
        String dataDir = args.length > 0 ? args[0] : "data";
        if (args.length > 0 && args[0].equalsIgnoreCase("--console")) {
            HospitalManagementSystem system = new HospitalManagementSystem(new InputHelper(System.in), "data");
            system.start();
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> HospitalGUI.start(dataDir));
    }
}
