package com.example.farmlink.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String studentId;
    private List<CartItem> items;
    private double totalAmount;

    public Cart() {
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public Cart(String studentId) {
        this.studentId = studentId;
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
