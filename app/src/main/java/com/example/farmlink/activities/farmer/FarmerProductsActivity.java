package com.example.farmlink.activities.farmer;



import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.adapters.ProductAdapter;
import com.example.farmlink.models.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FarmerProductsActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack;
    private EditText etSearch;
    private RecyclerView rvProducts;
    private ChipGroup chipFilter;
    private Chip chipAll, chipActive, chipLowStock, chipSoldOut;
    private BottomNavigationView bottomNavigation;

    // Data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();

    // Filter
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_products);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
        loadProducts();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvProducts = findViewById(R.id.rvProducts);
        chipFilter = findViewById(R.id.chipFilter);
        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chipLowStock = findViewById(R.id.chipLowStock);
        chipSoldOut = findViewById(R.id.chipSoldOut);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        // Farmer view = true (shows Edit button instead of Add to Cart)
        productAdapter = new ProductAdapter(filteredList, true);
        rvProducts.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "All";
            updateChipStyles(chipAll);
            applyFilters();
        });

        chipActive.setOnClickListener(v -> {
            currentFilter = "Active";
            updateChipStyles(chipActive);
            applyFilters();
        });

        chipLowStock.setOnClickListener(v -> {
            currentFilter = "LowStock";
            updateChipStyles(chipLowStock);
            applyFilters();
        });

        chipSoldOut.setOnClickListener(v -> {
            currentFilter = "SoldOut";
            updateChipStyles(chipSoldOut);
            applyFilters();
        });
    }

    private void updateChipStyles(Chip selectedChip) {
        // Reset all chips to default
        resetChipStyle(chipAll, "#E8F5E9", "#2E7D32");
        resetChipStyle(chipActive, "#E8F5E9", "#2E7D32");
        resetChipStyle(chipLowStock, "#FFF3E0", "#FF9800");
        resetChipStyle(chipSoldOut, "#FFEBEE", "#F44336");

        // Style selected chip
        selectedChip.setChipBackgroundColorResource(android.R.color.transparent);
        selectedChip.setBackgroundColor(getColor(R.color.farmer_green));
        selectedChip.setTextColor(getColor(android.R.color.white));
    }

    private void resetChipStyle(Chip chip, String bgColor, String textColor) {
        chip.setBackgroundColor(getColor(android.R.color.transparent));
        chip.setTextColor(getColor(android.R.color.black));
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        applyFilters();
    }

    private void applyFilters() {
        List<Product> tempList = new ArrayList<>();

        for (Product product : filteredList) {
            switch (currentFilter) {
                case "All":
                    tempList.add(product);
                    break;
                case "Active":
                    if (product.getQuantity() > 0) {
                        tempList.add(product);
                    }
                    break;
                case "LowStock":
                    if (product.getQuantity() > 0 && product.getQuantity() < 10) {
                        tempList.add(product);
                    }
                    break;
                case "SoldOut":
                    if (product.getQuantity() == 0) {
                        tempList.add(product);
                    }
                    break;
            }
        }

        filteredList.clear();
        filteredList.addAll(tempList);
        productAdapter.updateProducts(filteredList);
    }

    private void loadProducts() {
        String farmerId = mAuth.getCurrentUser().getUid();

        db.collection("products")
                .whereEqualTo("farmerId", farmerId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    productList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            product.setProductId(doc.getId());
                            productList.add(product);
                        }
                    }

                    filteredList.clear();
                    filteredList.addAll(productList);
                    productAdapter.updateProducts(filteredList);
                });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_products);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, FarmerDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_products) {
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
}