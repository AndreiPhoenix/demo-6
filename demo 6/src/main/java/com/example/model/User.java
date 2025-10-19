package com.example.model;

public class User {
    private String id;
    private String name;
    private String email;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}