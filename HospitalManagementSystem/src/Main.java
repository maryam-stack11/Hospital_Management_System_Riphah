/** Entry point. Optional argument: data folder (default "data"). */
public class Main {
    public static void main(String[] args) {
        String dataDir = args.length > 0 ? args[0] : "data";
        HospitalManagementSystem system = new HospitalManagementSystem(new InputHelper(System.in), dataDir);
        system.start();
    }
}
