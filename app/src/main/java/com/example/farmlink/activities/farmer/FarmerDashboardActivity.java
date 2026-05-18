package com.example.farmlink.activities.farmer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.Auth.LoginActivity;
import com.example.farmlink.R;
import com.example.farmlink.activities.student.ProductDetailActivity;
import com.example.farmlink.adapters.ProductAdapter;
import com.example.farmlink.models.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FarmerDashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvUserRole, tvDate, tvFarmLocation;
    private TextView tvProductsCount, tvOrdersCount, tvRatingCount, tvTotalEarnings;
    private RecyclerView rvRecentProducts;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private List<Product> recentProductsList;
    private ProductAdapter recentProductsAdapter;

    // Real stats from Firestore
    private int actualProductsCount = 0;
    private int actualOrdersCount = 0;
    private double actualTotalEarnings = 0.0;
    private double actualAverageRating = 0.0;

    // ListenerRegistrations for proper lifecycle management
    private ListenerRegistration productsCountListener;
    private ListenerRegistration ordersListener;
    private ListenerRegistration recentProductsListener;

    // State tracking
    private boolean isDataLoaded = false;
    private long onCreateTime = 0;

    // ==================== LIFECYCLE METHODS ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_dashboard);

        onCreateTime = System.currentTimeMillis();

        // Initialize
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Check authentication
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        // Restore state if available
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        initViews();
        setupGreeting();
        setupRecentProductsRecyclerView();
        loadFarmerData();

        // Only load data if not restored or data is stale
        if (!isDataLoaded || isDataStale()) {
            loadRealTimeStats();
            loadRecentProducts();
        }

        setupClickListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Re-attach listeners when activity becomes visible
        if (currentUser != null) {
            attachListeners();
        }
        // Refresh recent products to ensure up-to-date
        if (recentProductsList != null && recentProductsList.isEmpty()) {
            loadRecentProducts();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        refreshDashboardData();
        // Update greeting in case time changed
        updateGreetingBasedOnTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save current state before pausing
        saveCurrentState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detach listeners to save resources when not visible
        detachListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up all listeners to prevent memory leaks
        cleanupListeners();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save important data for configuration changes (rotation)
        outState.putInt("products_count", actualProductsCount);
        outState.putInt("orders_count", actualOrdersCount);
        outState.putDouble("total_earnings", actualTotalEarnings);
        outState.putDouble("avg_rating", actualAverageRating);
        outState.putBoolean("data_loaded", isDataLoaded);
        outState.putLong("last_update", System.currentTimeMillis());

        // Save products list
        if (recentProductsList != null && !recentProductsList.isEmpty()) {
            ArrayList<String> productIds = new ArrayList<>();
            for (Product product : recentProductsList) {
                productIds.add(product.getProductId());
            }
            outState.putStringArrayList("product_ids", productIds);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
    }

    // ==================== STATE MANAGEMENT ====================

    private void restoreState(Bundle savedInstanceState) {
        actualProductsCount = savedInstanceState.getInt("products_count", 0);
        actualOrdersCount = savedInstanceState.getInt("orders_count", 0);
        actualTotalEarnings = savedInstanceState.getDouble("total_earnings", 0.0);
        actualAverageRating = savedInstanceState.getDouble("avg_rating", 0.0);
        isDataLoaded = savedInstanceState.getBoolean("data_loaded", false);

        // Update UI with restored state
        if (tvProductsCount != null) tvProductsCount.setText(String.valueOf(actualProductsCount));
        if (tvOrdersCount != null) tvOrdersCount.setText(String.valueOf(actualOrdersCount));
        if (tvTotalEarnings != null) tvTotalEarnings.setText(String.format("R %.2f", actualTotalEarnings));
        if (tvRatingCount != null) tvRatingCount.setText(String.format("%.1f", actualAverageRating));
    }

    private void saveCurrentState() {
        // Save any pending state if needed
        // This can be used to save scroll position, etc.
    }

    private boolean isDataStale() {
        // Consider data stale after 5 minutes
        long currentTime = System.currentTimeMillis();
        return (currentTime - onCreateTime) > 5 * 60 * 1000;
    }

    private void refreshDashboardData() {
        // Quick refresh without full reload
        if (currentUser != null && recentProductsListener == null) {
            loadRecentProducts();
        }
    }

    // ==================== UI INITIALIZATION ====================

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvDate = findViewById(R.id.tvDate);
        tvFarmLocation = findViewById(R.id.tvFarmLocation);
        tvProductsCount = findViewById(R.id.tvProductsCount);
        tvOrdersCount = findViewById(R.id.tvOrdersCount);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        rvRecentProducts = findViewById(R.id.rvRecentProducts);
    }

    private void setupGreeting() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        if (tvDate != null) tvDate.setText(currentDate);

        updateGreetingBasedOnTime();
    }

    private void updateGreetingBasedOnTime() {
        int hour = new Date().getHours();
        String greeting;
        if (hour < 12) greeting = "Good morning, ";
        else if (hour < 16) greeting = "Good afternoon, ";
        else greeting = "Good evening, ";

        if (tvGreeting != null) tvGreeting.setText(greeting);
        if (tvUserRole != null) tvUserRole.setText("Farmer! 🚜");
    }

    private void setupRecentProductsRecyclerView() {
        recentProductsList = new ArrayList<>();
        recentProductsAdapter = new ProductAdapter(recentProductsList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onAddToCart(Product product) {
                // For farmer dashboard, navigate to edit product
                Intent intent = new Intent(FarmerDashboardActivity.this, EditProductActivity.class);
                intent.putExtra("product_id", product.getProductId());
                startActivity(intent);
            }

            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(FarmerDashboardActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getProductId());
                intent.putExtra("is_farmer_view", true);
                startActivity(intent);
            }
        });

        rvRecentProducts.setLayoutManager(new LinearLayoutManager(this));
        rvRecentProducts.setAdapter(recentProductsAdapter);
    }

    // ==================== DATA LOADING ====================

    private void loadFarmerData() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && tvFarmLocation != null) {
                        String location = doc.getString("location");
                        String farmName = doc.getString("farmName");

                        if (location != null && !location.isEmpty()) {
                            tvFarmLocation.setText("📍 " + location);
                            tvFarmLocation.setVisibility(View.VISIBLE);
                        } else if (farmName != null) {
                            tvFarmLocation.setText("🏠 " + farmName);
                            tvFarmLocation.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, "Failed to load farm data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attachListeners() {
        // Only attach if not already attached
        if (productsCountListener == null && currentUser != null) {
            loadRealTimeStats();
        }
        if (recentProductsListener == null && currentUser != null) {
            loadRecentProducts();
        }
    }

    private void detachListeners() {
        // Detach but don't destroy - can be reattached
        if (productsCountListener != null) {
            productsCountListener.remove();
            productsCountListener = null;
        }
        if (ordersListener != null) {
            ordersListener.remove();
            ordersListener = null;
        }
        if (recentProductsListener != null) {
            recentProductsListener.remove();
            recentProductsListener = null;
        }
    }

    private void cleanupListeners() {
        // Final cleanup - activity is being destroyed
        if (productsCountListener != null) {
            productsCountListener.remove();
            productsCountListener = null;
        }
        if (ordersListener != null) {
            ordersListener.remove();
            ordersListener = null;
        }
        if (recentProductsListener != null) {
            recentProductsListener.remove();
            recentProductsListener = null;
        }
    }

    private void loadRealTimeStats() {
        if (currentUser == null) return;

        // Load Products Count
        productsCountListener = db.collection("products")
                .whereEqualTo("farmerId", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null || isFinishing() || isDestroyed()) return;
                    if (value != null) {
                        actualProductsCount = value.size();
                        if (tvProductsCount != null) {
                            tvProductsCount.setText(String.valueOf(actualProductsCount));
                        }
                        isDataLoaded = true;
                    }
                });

        // Load Orders Count & Total Earnings
        ordersListener = db.collection("orders")
                .whereEqualTo("farmerId", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null || isFinishing() || isDestroyed()) return;
                    if (value != null) {
                        actualOrdersCount = value.size();
                        if (tvOrdersCount != null) {
                            tvOrdersCount.setText(String.valueOf(actualOrdersCount));
                        }

                        // Calculate total earnings
                        double total = 0;
                        for (var doc : value.getDocuments()) {
                            Double orderTotal = doc.getDouble("totalAmount");
                            if (orderTotal != null) total += orderTotal;
                        }
                        actualTotalEarnings = total;
                        if (tvTotalEarnings != null) {
                            tvTotalEarnings.setText(String.format("R %.2f", total));
                        }
                    }
                });

        // Load Average Rating (single fetch, no need real-time for this)
        db.collection("reviews")
                .whereEqualTo("farmerId", currentUser.getUid())
                .get()
                .addOnSuccessListener(reviews -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (reviews.isEmpty()) {
                        if (tvRatingCount != null) tvRatingCount.setText("0.0");
                        return;
                    }
                    double sum = 0;
                    for (var doc : reviews) {
                        Double rating = doc.getDouble("rating");
                        if (rating != null) sum += rating;
                    }
                    actualAverageRating = sum / reviews.size();
                    if (tvRatingCount != null) {
                        tvRatingCount.setText(String.format("%.1f", actualAverageRating));
                    }
                });
    }

    private void loadRecentProducts() {
        if (currentUser == null) return;

        // Remove old listener if exists
        if (recentProductsListener != null) {
            recentProductsListener.remove();
        }

        recentProductsListener = db.collection("products")
                .whereEqualTo("farmerId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null || isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (value != null) {
                        recentProductsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            product.setProductId(doc.getId());
                            recentProductsList.add(product);
                        }
                        if (recentProductsAdapter != null) {
                            recentProductsAdapter.notifyDataSetChanged();
                        }

                        // Update empty state
                        if (recentProductsList.isEmpty() && tvProductsCount != null) {
                            tvProductsCount.setText("0");
                        }
                    }
                });
    }

    // ==================== CLICK LISTENERS ====================

    private void setupClickListeners() {
        View cardAddProduct = findViewById(R.id.cardAddProduct);
        if (cardAddProduct != null) {
            cardAddProduct.setOnClickListener(v ->
                    startActivity(new Intent(this, AddProductActivity.class)));
        }

        View cardManageOrders = findViewById(R.id.cardManageOrders);
        if (cardManageOrders != null) {
            cardManageOrders.setOnClickListener(v ->
                    startActivity(new Intent(this, FarmerOrdersActivity.class)));
        }

        View cardInventory = findViewById(R.id.cardInventory);
        if (cardInventory != null) {
            cardInventory.setOnClickListener(v ->
                    startActivity(new Intent(this, FarmerProductsActivity.class)));
        }

        View cardEarnings = findViewById(R.id.cardEarnings);
        if (cardEarnings != null) {
            cardEarnings.setOnClickListener(v ->
                    Toast.makeText(this, "💰 Total Earnings: R " + String.format("%.2f", actualTotalEarnings), Toast.LENGTH_LONG).show());
        }

        View cardWeather = findViewById(R.id.cardWeather);
        if (cardWeather != null) {
            cardWeather.setOnClickListener(v ->
                    Toast.makeText(this, "🌤️ Weather feature coming soon!", Toast.LENGTH_SHORT).show());
        }

        View cardPrices = findViewById(R.id.cardPrices);
        if (cardPrices != null) {
            cardPrices.setOnClickListener(v ->
                    Toast.makeText(this, "📊 Market prices coming soon!", Toast.LENGTH_SHORT).show());
        }
    }

    // ==================== BOTTOM NAVIGATION ====================

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, FarmerProductsActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, FarmerOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, AddProductActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, FarmerProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // ==================== NAVIGATION ====================

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}