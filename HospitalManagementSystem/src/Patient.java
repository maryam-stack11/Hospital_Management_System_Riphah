/**
 * INHERITANCE: Patient extends Person and adds patient-specific attributes.
 * Constructor chaining with super(...) reuses the parent's constructor.
 */
public class Patient extends Person {
    private int age;
    private String gender;
    private String address;
    private String medicalCondition;

    public Patient(String id, String name, String contactNumber,
                   int age, String gender, String address, String medicalCondition) {
        super(id, name, contactNumber);
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.medicalCondition = medicalCondition;
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }

    /** Role-specific method: classifies the patient by age. */
    public String getAgeGroup() {
        if (age < 13) return "Child";
        if (age < 60) return "Adult";
        return "Senior";
    }

    @Override
    public String getRole() { return "Patient"; }

    /** METHOD OVERRIDING: Patient's own version of the abstract displayDetails(). */
    @Override
    public void displayDetails() {
        UI.thinLine();
        printCommonDetails();
        UI.field("Age", age + " (" + getAgeGroup() + ")");
        UI.field("Gender", gender);
        UI.field("Address", address);
        UI.field("Condition/Reason", medicalCondition);
        UI.thinLine();
    }

    public static String tableHeader() {
        return String.format("%-6s %-22s %-4s %-7s %-15s %-24s",
                "ID", "Name", "Age", "Gender", "Contact", "Condition");
    }

    public String toTableRow() {
        return String.format("%-6s %s %-4d %-7s %-15s %s", getId(), UI.fit(getName(), 22), age, gender,
                UI.fit(getContactNumber(), 15), UI.fit(medicalCondition, 24));
    }
}
