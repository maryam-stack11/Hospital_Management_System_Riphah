/**
 * Custom checked exception for business-rule and validation failures
 * (e.g. "doctor already booked", "invalid age"). Demonstrates exception handling:
 * the managers THROW it and the menus CATCH it and show a friendly message.
 */
public class HospitalException extends Exception {
    private static final long serialVersionUID = 1L;

    public HospitalException(String message) {
        super(message);
    }
}
