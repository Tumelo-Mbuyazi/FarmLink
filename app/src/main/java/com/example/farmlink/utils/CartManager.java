package com.example.farmlink.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.farmlink.models.Cart;
import com.example.farmlink.models.CartItem;
import com.example.farmlink.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {

    private static final String TAG = "CartManager";
    private static final String PREFS_NAME = "cart_prefs";
    private static final String KEY_CART_SYNCED = "cart_synced";
    private static final String COLLECTION_CARTS = "carts";

    private static CartManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final SharedPreferences sharedPreferences;
    private Cart currentCart;
    private ListenerRegistration cartListener;
    private OnCartChangedListener cartChangedListener;

    // Singleton pattern
    private CartManager(Context context) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentCart = new Cart();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    // Interface for cart change callbacks
    public interface OnCartChangedListener {
        void onCartChanged(Cart cart);
        void onCartItemAdded(CartItem item);
        void onCartItemRemoved(String productId);
        void onCartItemQuantityChanged(String productId, int newQuantity);
    }

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartChangedListener = listener;
    }

    // Initialize cart for current user
    public void initCart(OnCartLoadListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user logged in, using local cart");
            loadLocalCart();
            if (listener != null) listener.onCartLoaded(currentCart);
            return;
        }

        String userId = user.getUid();
        loadCartFromFirestore(userId, listener);
    }

    private void loadCartFromFirestore(String userId, OnCartLoadListener listener) {
        DocumentReference cartRef = db.collection(COLLECTION_CARTS).document(userId);

        cartRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentCart = documentSnapshot.toObject(Cart.class);
                if (currentCart == null) {
                    currentCart = new Cart(userId);
                } else {
                    currentCart.setStudentId(userId);
                }
                Log.d(TAG, "Cart loaded from Firestore: " + currentCart.getItems().size() + " items");
                syncLocalCartToFirestore(); // Merge any local changes
            } else {
                // No cart in Firestore, create new
                currentCart = new Cart(userId);
                Log.d(TAG, "New cart created for user: " + userId);
                saveCartToFirestore();
            }

            if (listener != null) listener.onCartLoaded(currentCart);
            if (cartChangedListener != null) cartChangedListener.onCartChanged(currentCart);

            // Setup real-time listener
            setupCartListener(userId);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading cart", e);
            loadLocalCart();
            if (listener != null) listener.onCartLoaded(currentCart);
        });
    }

    private void setupCartListener(String userId) {
        if (cartListener != null) {
            cartListener.remove();
        }

        cartListener = db.collection(COLLECTION_CARTS).document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Cart listener error", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Cart updatedCart = documentSnapshot.toObject(Cart.class);
                        if (updatedCart != null) {
                            updatedCart.setStudentId(userId);
                            currentCart = updatedCart;
                            saveLocalCart();
                            if (cartChangedListener != null) {
                                cartChangedListener.onCartChanged(currentCart);
                            }
                        }
                    }
                });
    }

    // Add product to cart
    public void addToCart(Product product, int quantity, OnCartActionListener listener) {
        if (product == null || quantity <= 0) {
            if (listener != null) listener.onError("Invalid product or quantity");
            return;
        }

        if (quantity > product.getQuantity()) {
            if (listener != null) listener.onError("Only " + product.getQuantity() + " units available");
            return;
        }

        // Check if product already in cart
        for (int i = 0; i < currentCart.getItems().size(); i++) {
            CartItem existing = currentCart.getItems().get(i);
            if (existing.getProductId().equals(product.getProductId())) {
                // Update quantity
                int newQuantity = existing.getQuantity() + quantity;
                if (newQuantity > product.getQuantity()) {
                    if (listener != null) listener.onError("Cannot exceed available stock");
                    return;
                }
                existing.setQuantity(newQuantity);
                updateCartTotal();
                saveToFirestore();
                if (cartChangedListener != null) {
                    cartChangedListener.onCartItemQuantityChanged(product.getProductId(), newQuantity);
                }
                if (listener != null) listener.onSuccess("Cart updated");
                return;
            }
        }

        // Add new item
        CartItem cartItem = new CartItem(product, quantity);
        cartItem.setCartItemId(product.getProductId() + "_" + System.currentTimeMillis());
        currentCart.getItems().add(cartItem);
        updateCartTotal();
        saveToFirestore();

        if (cartChangedListener != null) {
            cartChangedListener.onCartItemAdded(cartItem);
        }
        if (listener != null) listener.onSuccess("Added to cart");
    }

    // Remove item from cart
    public void removeFromCart(String productId, OnCartActionListener listener) {
        CartItem itemToRemove = null;
        for (CartItem item : currentCart.getItems()) {
            if (item.getProductId().equals(productId)) {
                itemToRemove = item;
                break;
            }
        }

        if (itemToRemove != null) {
            currentCart.getItems().remove(itemToRemove);
            updateCartTotal();
            saveToFirestore();
            if (cartChangedListener != null) {
                cartChangedListener.onCartItemRemoved(productId);
            }
            if (listener != null) listener.onSuccess("Removed from cart");
        } else {
            if (listener != null) listener.onError("Item not found in cart");
        }
    }

    // Update item quantity
    public void updateQuantity(String productId, int newQuantity, OnCartActionListener listener) {
        if (newQuantity <= 0) {
            removeFromCart(productId, listener);
            return;
        }

        for (CartItem item : currentCart.getItems()) {
            if (item.getProductId().equals(productId)) {
                if (newQuantity > item.getMaxAvailable()) {
                    if (listener != null) listener.onError("Only " + item.getMaxAvailable() + " available");
                    return;
                }
                item.setQuantity(newQuantity);
                updateCartTotal();
                saveToFirestore();
                if (cartChangedListener != null) {
                    cartChangedListener.onCartItemQuantityChanged(productId, newQuantity);
                }
                if (listener != null) listener.onSuccess("Quantity updated");
                return;
            }
        }

        if (listener != null) listener.onError("Item not found");
    }

    // Clear entire cart
    public void clearCart(OnCartActionListener listener) {
        currentCart.getItems().clear();
        currentCart.setTotalAmount(0.0);
        saveToFirestore();
        if (cartChangedListener != null) cartChangedListener.onCartChanged(currentCart);
        if (listener != null) listener.onSuccess("Cart cleared");
    }

    // Get current cart
    public Cart getCart() {
        return currentCart;
    }

    // Get item count
    public int getItemCount() {
        int count = 0;
        for (CartItem item : currentCart.getItems()) {
            count += item.getQuantity();
        }
        return count;
    }

    // Calculate cart total
    private void updateCartTotal() {
        double total = 0;
        for (CartItem item : currentCart.getItems()) {
            total += item.getSubtotal();
        }
        currentCart.setTotalAmount(total);
    }

    // Save cart to Firestore
    private void saveToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            saveLocalCart();
            return;
        }

        String userId = user.getUid();
        currentCart.setStudentId(userId);

        // Convert cart to map for Firestore
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("studentId", userId);
        cartData.put("totalAmount", currentCart.getTotalAmount());

        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (CartItem item : currentCart.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("cartItemId", item.getCartItemId());
            itemMap.put("productId", item.getProductId());
            itemMap.put("farmerId", item.getFarmerId());
            itemMap.put("farmerName", item.getFarmerName());
            itemMap.put("productName", item.getProductName());
            itemMap.put("productEmoji", item.getProductEmoji());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unit", item.getUnit());
            itemMap.put("maxAvailable", item.getMaxAvailable());
            itemMap.put("addedTimestamp", item.getAddedTimestamp());
            itemsList.add(itemMap);
        }
        cartData.put("items", itemsList);

        db.collection(COLLECTION_CARTS).document(userId)
                .set(cartData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cart saved to Firestore");
                    sharedPreferences.edit().putBoolean(KEY_CART_SYNCED, true).apply();
                    saveLocalCart(); // Backup locally
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save cart", e);
                    saveLocalCart(); // Save locally if Firestore fails
                });
    }

    private void saveCartToFirestore() {
        saveToFirestore();
    }

    // Local storage backup
    private void saveLocalCart() {
        try {
            String cartJson = convertCartToJson(currentCart);
            sharedPreferences.edit().putString("local_cart", cartJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving local cart", e);
        }
    }

    private void loadLocalCart() {
        String cartJson = sharedPreferences.getString("local_cart", null);
        if (cartJson != null) {
            currentCart = convertJsonToCart(cartJson);
            if (currentCart == null) {
                currentCart = new Cart();
            }
        } else {
            currentCart = new Cart();
        }
    }

    private void syncLocalCartToFirestore() {
        // If we have local items not synced, merge them
        String localCartJson = sharedPreferences.getString("local_cart", null);
        if (localCartJson != null && !sharedPreferences.getBoolean(KEY_CART_SYNCED, false)) {
            Cart localCart = convertJsonToCart(localCartJson);
            if (localCart != null && !localCart.getItems().isEmpty()) {
                // Merge local items into current cart
                for (CartItem localItem : localCart.getItems()) {
                    boolean exists = false;
                    for (CartItem existing : currentCart.getItems()) {
                        if (existing.getProductId().equals(localItem.getProductId())) {
                            existing.setQuantity(existing.getQuantity() + localItem.getQuantity());
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        currentCart.getItems().add(localItem);
                    }
                }
                updateCartTotal();
                saveToFirestore();
            }
        }
    }

    // Helper methods for JSON conversion (simplified)
    private String convertCartToJson(Cart cart) {
        // Implement Gson or manual JSON conversion
        // For simplicity, using a simple format
        StringBuilder sb = new StringBuilder();
        sb.append("{\"items\":[");
        for (int i = 0; i < cart.getItems().size(); i++) {
            CartItem item = cart.getItems().get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"productId\":\"").append(item.getProductId()).append("\",");
            sb.append("\"quantity\":").append(item.getQuantity()).append("}");
        }
        sb.append("],\"total\":").append(cart.getTotalAmount()).append("}");
        return sb.toString();
    }

    private Cart convertJsonToCart(String json) {
        // Implement proper JSON parsing (use Gson library ideally)
        Cart cart = new Cart();
        // Simplified - in production use Gson or JSONObject
        if (json.contains("\"items\"")) {
            // Parse logic here
        }
        return cart;
    }

    // Clean up listener
    public void cleanup() {
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
    }

    // Interfaces for callbacks
    public interface OnCartLoadListener {
        void onCartLoaded(Cart cart);
    }

    public interface OnCartActionListener {
        void onSuccess(String message);
        void onError(String error);
    }
}