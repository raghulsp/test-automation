package com.framework.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request body for user-related API calls.
 * Null fields are omitted from JSON serialisation so optional fields are not sent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest {

    private String name;
    private String job;
    private String email;
    private String password;

    private UserRequest() {}

    public static Builder builder() { return new Builder(); }

    public String getName()     { return name; }
    public String getJob()      { return job; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    public static class Builder {
        private final UserRequest req = new UserRequest();

        public Builder name(String name)         { req.name = name;         return this; }
        public Builder job(String job)           { req.job = job;           return this; }
        public Builder email(String email)       { req.email = email;       return this; }
        public Builder password(String password) { req.password = password; return this; }

        public UserRequest build() { return req; }
    }
}
