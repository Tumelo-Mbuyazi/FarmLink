package com.example.farmlink.activities.student;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.models.CartItem;
import com.example.farmlink.utils.CartManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack;
    private RecyclerView rvOrderItems;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private TextInputEditText etFullName, etPhone, etAddress, etInstructions;
    private RadioGroup radioGroupPayment;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;

    // Data
    private CartManager cartManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<CartItem> cartItems;
    private double subtotal = 0;
    private static final double DELIVERY_FEE = 25.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_checkout);

        cartManager = CartManager.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadCartData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etInstructions = findViewById(R.id.etInstructions);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);

        tvDeliveryFee.setText(String.format("R%.2f", DELIVERY_FEE));
    }

    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCartData() {
        cartManager.initCart(cart -> {
            if (cart != null) {
                cartItems = cart.getItems();
                updateOrderSummary();
            }
        });
    }

    private void updateOrderSummary() {
        subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }

        double total = subtotal + DELIVERY_FEE;

        tvSubtotal.setText(String.format("R%.2f", subtotal));
        tvTotal.setText(String.format("R%.2f", total));

        // Set adapter
        OrderSummaryAdapter adapter = new OrderSummaryAdapter(cartItems);
        rvOrderItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        // Validate inputs
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full name required");
            etFullName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number required");
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Delivery address required");
            etAddress.requestFocus();
            return;
        }

        int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = "";
        if (selectedPaymentId == R.id.radioCard) {
            paymentMethod = "Credit/Debit Card";
        } else if (selectedPaymentId == R.id.radioCash) {
            paymentMethod = "Cash on Delivery";
        } else if (selectedPaymentId == R.id.radioMobile) {
            paymentMethod = "Mobile Money";
        }

        String instructions = etInstructions.getText().toString().trim();
        if (instructions.isEmpty()) {
            instructions = "None";
        }

        showProgress(true);

        // Create order object
        String orderId = UUID.randomUUID().toString();
        String studentId = mAuth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();
        String orderStatus = "pending";
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("studentId", studentId);
        order.put("studentName", fullName);
        order.put("studentPhone", phone);
        order.put("deliveryAddress", address);
        order.put("instructions", instructions);
        order.put("paymentMethod", paymentMethod);
        order.put("subtotal", subtotal);
        order.put("deliveryFee", DELIVERY_FEE);
        order.put("totalAmount", subtotal + DELIVERY_FEE);
        order.put("status", orderStatus);
        order.put("timestamp", timestamp);
        order.put("date", date);

        // Add order items
        List<Map<String, Object>> orderItems = new java.util.ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("productId", item.getProductId());
            orderItem.put("productName", item.getProductName());
            orderItem.put("productEmoji", item.getProductEmoji());
            orderItem.put("farmerId", item.getFarmerId());
            orderItem.put("farmerName", item.getFarmerName());
            orderItem.put("price", item.getPrice());
            orderItem.put("quantity", item.getQuantity());
            orderItem.put("unit", item.getUnit());
            orderItem.put("subtotal", item.getSubtotal());
            orderItems.add(orderItem);
        }
        order.put("items", orderItems);

        // Save to Firestore
        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    // Update product quantities
                    updateProductQuantities();

                    // Clear cart
                    cartManager.clearCart(new CartManager.OnCartActionListener() {
                        @Override
                        public void onSuccess(String message) {
                            showProgress(false);
                            Toast.makeText(CheckoutActivity.this,
                                    "✅ Order placed successfully!\nOrder ID: " + orderId.substring(0, 8),
                                    Toast.LENGTH_LONG).show();

                            // Navigate to order confirmation
                            Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                            intent.putExtra("order_id", orderId);
                            intent.putExtra("total_amount", subtotal + DELIVERY_FEE);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            showProgress(false);
                            Toast.makeText(CheckoutActivity.this,
                                    "Order placed but cart clear failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(CheckoutActivity.this,
                            "Failed to place order: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProductQuantities() {
        for (CartItem item : cartItems) {
            db.collection("products").document(item.getProductId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            int currentQty = doc.getLong("quantity").intValue();
                            int newQty = currentQty - item.getQuantity();

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("quantity", newQty);
                            if (newQty <= 0) {
                                updates.put("status", "out_of_stock");
                            }

                            db.collection("products").document(item.getProductId())
                                    .update(updates);
                        }
                    });
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setEnabled(!show);
        btnPlaceOrder.setText(show ? "Placing Order..." : "✅ Place Order");
    }

    // ==================== ORDER SUMMARY ADAPTER ====================

    class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {
        private List<CartItem> items;

        OrderSummaryAdapter(List<CartItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_summary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = items.get(position);
            holder.tvEmoji.setText(item.getProductEmoji());
            holder.tvName.setText(item.getProductName());
            holder.tvQuantity.setText("Qty: " + item.getQuantity());
            holder.tvPrice.setText(String.format("R%.2f", item.getSubtotal()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvQuantity, tvPrice;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvEmoji);
                tvName = itemView.findViewById(R.id.tvName);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }
    }
}