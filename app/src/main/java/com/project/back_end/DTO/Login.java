package com.project.back_end.DTO;

public class Login {
    
    private String identifier;
    private String password;

    /**
     * Default constructor for Login DTO.
     * Used for deserialization of login request from the client.
     */
    public Login() {
    }

    /**
     * Constructor that initializes login credentials.
     * 
     * @param identifier The unique identifier of the user (email or username)
     * @param password The password provided by the user
     */
    public Login(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    /**
     * Gets the user identifier (email or username).
     * 
     * @return The identifier value
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the user identifier (email or username).
     * 
     * @param identifier The identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the user password.
     * 
     * @return The password value
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user password.
     * 
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
