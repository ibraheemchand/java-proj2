// Java version of Admin class
// Demonstrates Inheritance and Interface implementation
public class Admin extends BankEntity implements Authenticatable {
    private String password;

    public Admin(String username, String password) {
        super(username, username);
        this.password = password;
    }

    @Override
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    @Override
    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    @Override
    public String getEntityType() {
        return "Admin";
    }

    @Override
    public String displayInfo() {
        return String.format("Admin{id='%s', name='%s', entityType='%s'}",
            getId(), getName(), getEntityType());
    }

    @Override
    public String toString() {
        return displayInfo();
    }
}

