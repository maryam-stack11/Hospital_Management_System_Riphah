/**
 * ABSTRACTION + ENCAPSULATION: Person is an abstract parent class holding the
 * details shared by every person in the hospital (ID, name, contact number).
 * It cannot be instantiated directly - only Patient and Doctor (INHERITANCE) can.
 * Fields are private; they are reached only through getters/setters.
 */
public abstract class Person {
    private final String id;          // an ID never changes once created
    private String name;
    private String contactNumber;

    public Person(String id, String name, String contactNumber) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getContactNumber() { return contactNumber; }

    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    /** Abstract: every kind of person must say what role it plays. */
    public abstract String getRole();

    /** Abstract: each child class OVERRIDES this (POLYMORPHISM). */
    public abstract void displayDetails();

    /** Shared helper so children do not repeat the common printing code. */
    protected void printCommonDetails() {
        UI.field(getRole() + " ID", id);
        UI.field("Name", name);
        UI.field("Contact Number", contactNumber);
    }

    @Override
    public String toString() {
        return getRole() + " " + id + " - " + name;
    }
}
