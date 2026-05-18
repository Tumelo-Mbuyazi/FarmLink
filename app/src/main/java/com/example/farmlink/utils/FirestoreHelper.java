package com.example.farmlink.utils;


import com.example.farmlink.models.Cart;
import com.example.farmlink.models.Order;
import com.example.farmlink.models.Product;
import com.example.farmlink.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private static FirestoreHelper instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    // User Collection
    public void saveUser(User user, OnCompleteListener<Void> listener) {
        db.collection("users").document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    public void getUser(String userId, OnCompleteListener<User> listener) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    User user = document.toObject(User.class);
                    listener.onSuccess(user);
                })
                .addOnFailureListener(listener::onFailure);
    }

    // Products Collection
    public void addProduct(Product product, OnCompleteListener<String> listener) {
        String productId = db.collection("products").document().getId();
        product.setProductId(productId);

        db.collection("products").document(productId)
                .set(product)
                .addOnSuccessListener(aVoid -> listener.onSuccess(productId))
                .addOnFailureListener(listener::onFailure);
    }

    public Query getFarmerProducts(String farmerId) {
        return db.collection("products")
                .whereEqualTo("farmerId", farmerId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getAllProducts() {
        return db.collection("products")
                .whereEqualTo("status", "active")
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public void updateProduct(Product product, OnCompleteListener<Void> listener) {
        db.collection("products").document(product.getProductId())
                .update(
                        "name", product.getName(),
                        "price", product.getPrice(),
                        "quantity", product.getQuantity(),
                        "description", product.getDescription(),
                        "status", product.getStatus()
                )
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    public void deleteProduct(String productId, OnCompleteListener<Void> listener) {
        db.collection("products").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    // Orders Collection
    public void createOrder(Order order, OnCompleteListener<String> listener) {
        String orderId = db.collection("orders").document().getId();
        order.setOrderId(orderId);
        order.setTimestamp(System.currentTimeMillis());

        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    // Update product quantities - FIXED: Use Map instead of OrderItem
                    updateProductQuantities(order);
                    listener.onSuccess(orderId);
                })
                .addOnFailureListener(listener::onFailure);
    }

    // Helper method to update product quantities from order
    private void updateProductQuantities(Order order) {
        List<Map<String, Object>> items = order.getItems();

        if (items == null || items.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : items) {
            // Extract productId
            String productId = (String) item.get("productId");

            // Extract quantity (handle different number types)
            int quantity = 0;
            Object quantityObj = item.get("quantity");
            if (quantityObj instanceof Integer) {
                quantity = (Integer) quantityObj;
            } else if (quantityObj instanceof Long) {
                quantity = ((Long) quantityObj).intValue();
            } else if (quantityObj instanceof Double) {
                quantity = ((Double) quantityObj).intValue();
            }

            if (productId != null && quantity > 0) {
                updateProductQuantity(productId, -quantity);
            }
        }
    }

    public Query getFarmerOrders(String farmerId) {
        return db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getStudentOrders(String studentId) {
        return db.collection("orders")
                .whereEqualTo("studentId", studentId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public void updateOrderStatus(String orderId, String status, OnCompleteListener<Void> listener) {
        db.collection("orders").document(orderId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    // Cart Operations
    public void saveCart(Cart cart, OnCompleteListener<Void> listener) {
        db.collection("carts").document(cart.getStudentId())
                .set(cart)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onFailure);
    }

    public void getCart(String studentId, OnCompleteListener<Cart> listener) {
        db.collection("carts").document(studentId)
                .get()
                .addOnSuccessListener(document -> {
                    Cart cart = document.toObject(Cart.class);
                    if (cart == null) cart = new Cart();
                    listener.onSuccess(cart);
                })
                .addOnFailureListener(listener::onFailure);
    }

    // Helper method to update product quantity
    private void updateProductQuantity(String productId, int delta) {
        db.collection("products").document(productId)
                .update("quantity", com.google.firebase.firestore.FieldValue.increment(delta));
    }

    // Interface for callbacks
    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}