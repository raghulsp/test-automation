package com.framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the user object returned by the API.
 * Unknown fields are ignored so the class stays stable across API additions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {

    private int    id;
    private String name;
    private String job;
    private String email;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public int    getId()        { return id; }
    public String getName()      { return name; }
    public String getJob()       { return job; }
    public String getEmail()     { return email; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
