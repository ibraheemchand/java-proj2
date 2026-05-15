/**
 * Abstract base class for all bank entities
 * Demonstrates Abstraction in OOP
 */
public abstract class BankEntity {
    private String id;
    private String name;

    public BankEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Abstract method - each subclass must implement
     * Demonstrates Polymorphism
     */
    public abstract String getEntityType();

    /**
     * Abstract method - returns formatted entity information
     * Demonstrates Polymorphism
     */
    public abstract String displayInfo();

    @Override
    public String toString() {
        return displayInfo();
    }
}
