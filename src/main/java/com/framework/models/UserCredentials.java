package com.framework.models;

/**
 * Immutable credentials produced by the API and consumed by UI login steps.
 */
public class UserCredentials {

    private final String username;
    private final String password;

    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return "UserCredentials{username='" + username + "'}";
    }
}
