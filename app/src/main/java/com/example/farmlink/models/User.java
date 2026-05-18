package com.example.farmlink.models;

public class User {
    private String userId;
    private String email;
    private String role;
    private String name;
    private String phone;
    private String location;
    private long createdAt;

    // Required empty constructor for Firestore
    public User() {}

    public User(String userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
