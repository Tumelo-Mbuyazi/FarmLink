package com.example.farmlink.activities.student;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyOrdersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private TextView tvEmptyOrders;
    private MaterialButton btnContinueShopping;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private OrdersAdapter ordersAdapter;
    private List<Order> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
    }

    private void setupRecyclerView() {
        ordersAdapter = new OrdersAdapter(orderList, order -> {
            // Navigate to order details
            Intent intent = new Intent(MyOrdersActivity.this, OrderTrackingActivity.class);
            intent.putExtra("order_id", order.getOrderId());
            startActivity(intent);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(ordersAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(MyOrdersActivity.this, StudentMarketplaceActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadOrders() {
        String studentId = mAuth.getCurrentUser().getUid();
        showProgress(true);

        db.collection("orders")
                .whereEqualTo("studentId", studentId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    showProgress(false);
                    if (error != null) {
                        Toast.makeText(this, "Error loading orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    orderList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = new Order();
                            order.setOrderId(doc.getString("orderId"));
                            order.setStudentName(doc.getString("studentName"));
                            order.setTotalAmount(doc.getDouble("totalAmount"));
                            order.setStatus(doc.getString("status"));
                            order.setDate(doc.getString("date"));
                            order.setTimestamp(doc.getLong("timestamp"));
                            order.setPaymentMethod(doc.getString("paymentMethod"));
                            order.setDeliveryAddress(doc.getString("deliveryAddress"));
                            order.setItems((List<Map<String, Object>>) doc.get("items"));
                            orderList.add(order);
                        }
                        ordersAdapter.notifyDataSetChanged();
                        rvOrders.setVisibility(View.VISIBLE);
                        tvEmptyOrders.setVisibility(View.GONE);
                    } else {
                        rvOrders.setVisibility(View.GONE);
                        tvEmptyOrders.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // Order Model Class
    public static class Order {
        private String orderId;
        private String studentName;
        private Double totalAmount;
        private String status;
        private String date;
        private Long timestamp;
        private String paymentMethod;
        private String deliveryAddress;
        private List<Map<String, Object>> items;

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
        public List<Map<String, Object>> getItems() { return items; }
        public void setItems(List<Map<String, Object>> items) { this.items = items; }

        public String getStatusColor() {
            switch (status) {
                case "delivered": return "#4CAF50";
                case "shipped": return "#2196F3";
                case "pending": return "#FF9800";
                case "cancelled": return "#F44336";
                default: return "#757575";
            }
        }

        public String getStatusIcon() {
            switch (status) {
                case "delivered": return "✅";
                case "shipped": return "🚚";
                case "pending": return "⏳";
                case "cancelled": return "❌";
                default: return "📦";
            }
        }
    }

    // Orders Adapter
    static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        private List<Order> orders;
        private OnOrderClickListener listener;

        interface OnOrderClickListener {
            void onOrderClick(Order order);
        }

        OrdersAdapter(List<Order> orders, OnOrderClickListener listener) {
            this.orders = orders;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.tvOrderId.setText("#" + order.getOrderId().substring(0, 8));
            holder.tvDate.setText(order.getDate());
            holder.tvTotal.setText(String.format("R%.2f", order.getTotalAmount()));
            holder.tvStatus.setText(order.getStatus().toUpperCase());
            holder.tvStatus.setBackgroundColor(android.graphics.Color.parseColor(order.getStatusColor()));
            holder.tvItemsCount.setText(order.getItems() != null ? order.getItems().size() + " items" : "0 items");

            holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvDate, tvTotal, tvStatus, tvItemsCount;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            }
        }
    }
}