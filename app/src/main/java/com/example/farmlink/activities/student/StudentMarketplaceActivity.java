package com.example.farmlink.activities.student;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.Auth.LoginActivity;
import com.example.farmlink.R;
import com.example.farmlink.StudentAIActivity;
import com.example.farmlink.adapters.ProductAdapter;
import com.example.farmlink.models.Cart;
import com.example.farmlink.models.CartItem;
import com.example.farmlink.models.Product;
import com.example.farmlink.utils.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.example.farmlink.StudentAIActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class StudentMarketplaceActivity extends AppCompatActivity {

    private static final String TAG = "StudentMarketplace";
    private static final String PREFS_NAME = "FarmLinkPrefs";

    // Views
    private EditText etSearch;
    private RecyclerView rvProducts;
    private TextView tvResults;
    private ImageView btnBack, btnCart;
    private BottomNavigationView bottomNavigation;
    private MaterialButton btnConnect;

    // Sort Buttons
    private TextView sortDistance, sortPrice, sortLatest;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FloatingActionButton fabAI;

    // Cart Manager
    private CartManager cartManager;

    // SharedPreferences
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    // Lifecycle Variables
    private long startTime;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "========== ON CREATE ==========");
        setContentView(R.layout.student_marketplace);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize CartManager
        cartManager = CartManager.getInstance(this);
        cartManager.initCart(cart -> {
            Log.d(TAG, "Cart loaded with " + cartManager.getItemCount() + " items");
            updateCartBadge();
        });

        // Set cart change listener
        cartManager.setOnCartChangedListener(new CartManager.OnCartChangedListener() {
            @Override
            public void onCartChanged(Cart cart) {
                updateCartBadge();
            }

            @Override
            public void onCartItemAdded(CartItem item) {
                updateCartBadge();
            }

            @Override
            public void onCartItemRemoved(String productId) {
                updateCartBadge();
            }

            @Override
            public void onCartItemQuantityChanged(String productId, int newQuantity) {
                updateCartBadge();
            }
        });

        initViews();
        setupFAB();
        setupRecyclerView();  // ← MUST BE CALLED BEFORE loadProductsFromFirestore
        setupClickListeners();
        setupBottomNavigation();
        setupBackNavigation();
        loadProductsFromFirestore();

        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }

        checkAuthState();
    }

    private void setupFAB() {
        if (fabAI != null) {
            fabAI.setOnClickListener(v -> {
                Toast.makeText(StudentMarketplaceActivity.this, "Opening AI Assistant...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StudentMarketplaceActivity.this, StudentAIActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "FAB is NULL! Check layout ID");
        }
    }
    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvProducts = findViewById(R.id.rvProducts);
        tvResults = findViewById(R.id.tvResults);
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnConnect = findViewById(R.id.btnConnect);
        sortDistance = findViewById(R.id.sortDistance);
        sortPrice = findViewById(R.id.sortPrice);
        sortLatest = findViewById(R.id.sortLatest);
        fabAI = findViewById(R.id.fabAI);
    }

    private void setupRecyclerView() {
        // FIXED: Create adapter with productList and listener
        productAdapter = new ProductAdapter(filteredList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onAddToCart(Product product) {
                addToCart(product);
            }

            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(StudentMarketplaceActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getProductId());
                intent.putExtra("product", gson.toJson(product));
                startActivity(intent);
            }
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });
        }

        btnCart.setOnClickListener(v -> {
            Log.d(TAG, "Cart button clicked");
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        btnConnect.setOnClickListener(v -> {
            Log.d(TAG, "Connect button clicked");
            Intent intent = new Intent(this, StudentConnectActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        sortDistance.setOnClickListener(v -> {
            Log.d(TAG, "Sort by Distance clicked");
            sortProducts("distance");
        });

        sortPrice.setOnClickListener(v -> {
            Log.d(TAG, "Sort by Price clicked");
            sortProducts("price");
        });

        sortLatest.setOnClickListener(v -> {
            Log.d(TAG, "Sort by Latest clicked");
            sortProducts("latest");
        });
    }

    private void filterProducts(String query) {
        Log.d(TAG, "filterProducts: Filtering with query: " + query);
        filteredList.clear();
        for (Product product : productList) {
            if (query.isEmpty() ||
                    product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getFarmerName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateProducts(filteredList);
        tvResults.setText(filteredList.size() + " products available");
    }

    private void sortProducts(String type) {
        Log.d(TAG, "sortProducts: Sorting by " + type);
        if (type.equals("price")) {
            filteredList.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        } else if (type.equals("distance")) {
            filteredList.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        } else if (type.equals("latest")) {
            filteredList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }
        productAdapter.updateProducts(filteredList);
        updateSortUI(type);
        Toast.makeText(this, "Sorted by " + type, Toast.LENGTH_SHORT).show();
    }

    private void updateSortUI(String selected) {
        // Reset all
        sortDistance.setBackgroundResource(0);
        sortPrice.setBackgroundResource(0);
        sortLatest.setBackgroundResource(0);
        sortDistance.setTextColor(getColor(android.R.color.darker_gray));
        sortPrice.setTextColor(getColor(android.R.color.darker_gray));
        sortLatest.setTextColor(getColor(android.R.color.darker_gray));

        // Highlight selected
        if (selected.equals("distance")) {
            sortDistance.setBackgroundColor(getColor(R.color.farmer_green));
            sortDistance.setTextColor(getColor(android.R.color.white));
        } else if (selected.equals("price")) {
            sortPrice.setBackgroundColor(getColor(R.color.farmer_green));
            sortPrice.setTextColor(getColor(android.R.color.white));
        } else if (selected.equals("latest")) {
            sortLatest.setBackgroundColor(getColor(R.color.farmer_green));
            sortLatest.setTextColor(getColor(android.R.color.white));
        }
    }

    private void loadProductsFromFirestore() {
        Log.d(TAG, "loadProductsFromFirestore: Loading products from Firestore");

        db.collection("products")
                .whereEqualTo("status", "active")
                .whereGreaterThan("quantity", 0)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading products", error);
                        Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    productList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Product product = doc.toObject(Product.class);
                        product.setProductId(doc.getId());
                        productList.add(product);
                        Log.d(TAG, "Loaded product: " + product.getName());
                    }
                    filterProducts("");
                });
    }

    private void addToCart(Product product) {
        Log.d(TAG, "addToCart: Adding " + product.getName() + " to cart");

        cartManager.addToCart(product, 1, new CartManager.OnCartActionListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(StudentMarketplaceActivity.this, message, Toast.LENGTH_SHORT).show();
                updateCartBadge();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StudentMarketplaceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartBadge() {
        int count = cartManager.getItemCount();
        Log.d(TAG, "updateCartBadge: Cart item count = " + count);

        if (count > 0) {
            btnCart.setImageResource(R.drawable.ic_cart_with_badge);
        } else {
            btnCart.setImageResource(R.drawable.ic_cart_white);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_marketplace);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, StudentDashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_learning) {
                startActivity(new Intent(this, StudentLearningActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_marketplace) {
                return true;
            } else if (id == R.id.nav_connect) {
                startActivity(new Intent(this, StudentConnectActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, StudentProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: Navigating back");
                finish();
            }
        });
    }

    private void updateLastVisited() {
        prefs.edit().putLong("last_visited_marketplace", System.currentTimeMillis()).apply();
    }

    private void saveSessionDuration(long duration) {
        long totalTime = prefs.getLong("total_marketplace_time", 0);
        prefs.edit().putLong("total_marketplace_time", totalTime + duration).apply();
    }

    private void refreshData() {
        Log.d(TAG, "refreshData: Refreshing marketplace data");
        loadProductsFromFirestore();
        updateCartBadge();
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("search_query")) {
            etSearch.setText(savedInstanceState.getString("search_query"));
        }
        if (savedInstanceState.containsKey("results_text")) {
            tvResults.setText(savedInstanceState.getString("results_text"));
        }
    }

    private void checkAuthState() {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to login");
            navigateToLogin();
        } else {
            Log.d(TAG, "User logged in: " + mAuth.getCurrentUser().getEmail());
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void cleanup() {
        Log.d(TAG, "cleanup: Cleaning up resources");
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "========== ON START ==========");
        if (mAuth.getCurrentUser() == null) {
            navigateToLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "========== ON RESUME ==========");
        startTime = System.currentTimeMillis();
        updateLastVisited();
        refreshData();
        updateCartBadge();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "========== ON PAUSE ==========");
        long sessionDuration = System.currentTimeMillis() - startTime;
        saveSessionDuration(sessionDuration);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "========== ON STOP ==========");
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "========== ON DESTROY ==========");
        if (cartManager != null) {
            cartManager.cleanup();
        }
        cleanup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "========== ON RESTART ==========");
        refreshData();
        updateCartBadge();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (etSearch != null) {
            outState.putString("search_query", etSearch.getText().toString());
        }
        if (tvResults != null) {
            outState.putString("results_text", tvResults.getText().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreSavedState(savedInstanceState);
    }
}