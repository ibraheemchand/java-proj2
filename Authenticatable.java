/**
 * Interface for authenticatable entities
 * Demonstrates Interface design pattern
 */
public interface Authenticatable {
    /**
     * Authenticate with a password
     * @param password the password to verify
     * @return true if password matches, false otherwise
     */
    boolean authenticate(String password);

    /**
     * Set a new password
     * @param newPassword the new password to set
     */
    void setPassword(String newPassword);
}
