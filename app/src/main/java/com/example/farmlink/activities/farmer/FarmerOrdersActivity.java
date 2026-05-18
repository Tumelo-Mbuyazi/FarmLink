package com.example.farmlink.activities.farmer;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.adapters.FarmerOrdersAdapter;
import com.example.farmlink.models.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FarmerOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TabLayout tabOrders;
    private BottomNavigationView bottomNavigation;
    private FarmerOrdersAdapter ordersAdapter;
    private List<Order> orderList = new ArrayList<>();
    private List<Order> filteredList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_orders);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupTabs();
        setupBottomNavigation();
        loadOrders();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        tabOrders = findViewById(R.id.tabOrders);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new FarmerOrdersAdapter(filteredList, order -> {
            // Handle order click
            Toast.makeText(this, "Order: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
            // TODO: Open order detail activity
        });
        rvOrders.setAdapter(ordersAdapter);
    }

    private void setupTabs() {
        tabOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    currentStatus = tab.getText().toString();
                    filterOrders();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterOrders() {
        filteredList.clear();
        if (currentStatus.equals("All")) {
            filteredList.addAll(orderList);
        } else {
            for (Order order : orderList) {
                String orderStatus = order.getStatus();
                if (orderStatus != null && orderStatus.equalsIgnoreCase(currentStatus)) {
                    filteredList.add(order);
                }
            }
        }
        ordersAdapter.updateOrders(filteredList);
    }

    private void loadOrders() {
        String farmerId = mAuth.getCurrentUser().getUid();

        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    orderList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                            boolean hasFarmersProduct = false;

                            if (items != null) {
                                for (Map<String, Object> item : items) {
                                    String itemFarmerId = (String) item.get("farmerId");
                                    if (itemFarmerId != null && itemFarmerId.equals(farmerId)) {
                                        hasFarmersProduct = true;
                                        break;
                                    }
                                }
                            }

                            if (hasFarmersProduct) {
                                Order order = new Order();
                                order.setOrderId(doc.getId());
                                order.setStudentName(doc.getString("studentName"));
                                order.setStudentPhone(doc.getString("studentPhone"));
                                order.setDeliveryAddress(doc.getString("deliveryAddress"));
                                Double total = doc.getDouble("totalAmount");
                                order.setTotalAmount(total != null ? total : 0.0);
                                order.setStatus(doc.getString("status"));
                                order.setDate(doc.getString("date"));
                                Long timestamp = doc.getLong("timestamp");
                                order.setTimestamp(timestamp != null ? timestamp : 0);
                                order.setPaymentMethod(doc.getString("paymentMethod"));
                                order.setItems(items);
                                orderList.add(order);
                            }
                        }
                    }
                    filterOrders();
                });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_orders);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, FarmerDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, FarmerProductsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, AddProductActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, FarmerProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}