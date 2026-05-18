package com.example.farmlink.activities.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.Auth.LoginActivity;
import com.example.farmlink.R;
import com.example.farmlink.StudentAIActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class StudentConnectActivity extends AppCompatActivity {

    private static final String TAG = "StudentConnect";
    private static final String PREFS_NAME = "FarmLinkPrefs";

    // Views
    private ImageView btnBack;
    private FloatingActionButton fabAI;
    private MaterialCardView cardAskFarmer, cardFarmVisit, cardForum;
    private RecyclerView rvLiveSessions, rvTopFarmers, rvRecentQuestions;
    private BottomNavigationView bottomNavigation;

    // Firebase
    private FirebaseAuth mAuth;

    // Lifecycle Variables
    private long startTime;
    private boolean canExit = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "========== ON CREATE ==========");
        setContentView(R.layout.student_connect);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupFAB();
        setupClickListeners();
        setupRecyclerViews();
        setupBottomNavigation();
        setupBackNavigation();

        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }

        checkAuthState();
    }
    private void setupFAB() {
        if (fabAI != null) {
            fabAI.setOnClickListener(v -> {
                Toast.makeText(StudentConnectActivity.this, "Opening AI Assistant...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StudentConnectActivity.this, StudentAIActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "FAB is NULL! Check layout ID");
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: Back button pressed");
                if (canExit) {
                    finish();
                } else {
                    canExit = true;
                    Toast.makeText(StudentConnectActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(() -> canExit = false, 2000);
                }
            }
        });
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
        cleanup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "========== ON RESTART ==========");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "========== ON SAVE INSTANCE STATE ==========");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "========== ON RESTORE INSTANCE STATE ==========");
        restoreSavedState(savedInstanceState);
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");

        // Navigation
        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAI = findViewById(R.id.fabAI);

        // Main Cards
        cardAskFarmer = findViewById(R.id.cardAskFarmer);
        cardFarmVisit = findViewById(R.id.cardFarmVisit);
        cardForum = findViewById(R.id.cardForum);

        // RecyclerViews
        rvLiveSessions = findViewById(R.id.rvLiveSessions);
        rvTopFarmers = findViewById(R.id.rvTopFarmers);
        rvRecentQuestions = findViewById(R.id.rvRecentQuestions);
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners");

        // Back Button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // Main Cards
        cardAskFarmer.setOnClickListener(v -> {
            Log.d(TAG, "Ask a Farmer card clicked");
            Intent intent = new Intent(this, AskFarmerActivity.class);
            startActivity(intent);
        });

        cardFarmVisit.setOnClickListener(v -> {
            Log.d(TAG, "Request Farm Visit card clicked");
            Intent intent = new Intent(this, RequestFarmVisitActivity.class);
            startActivity(intent);
        });

        cardForum.setOnClickListener(v -> {
            Log.d(TAG, "Discussion Forum card clicked");
            Toast.makeText(this, "Discussion Forum - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerViews() {
        Log.d(TAG, "setupRecyclerViews: Setting up RecyclerViews");

        // Live Sessions - Horizontal Scroll
        rvLiveSessions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLiveSessions.setNestedScrollingEnabled(false);

        // Top Farmers - Horizontal Scroll
        rvTopFarmers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopFarmers.setNestedScrollingEnabled(false);

        // Recent Questions - Vertical List
        rvRecentQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentQuestions.setNestedScrollingEnabled(false);

        // Load Data
        loadLiveSessions();
        loadTopFarmers();
        loadRecentQuestions();
    }

    private void loadLiveSessions() {
        List<LiveSession> sessions = new ArrayList<>();
        sessions.add(new LiveSession("🌾", "Soil Health Workshop", "Today at 4:00 PM", "Dr. Wilson", "🔴 Live"));
        sessions.add(new LiveSession("🐛", "Pest Control Q&A", "Tomorrow at 10:00 AM", "John Farmer", "📅 Upcoming"));
        sessions.add(new LiveSession("💧", "Irrigation Techniques", "Fri at 2:00 PM", "Sarah Green", "📅 Upcoming"));
        sessions.add(new LiveSession("🌱", "Organic Farming", "Sat at 11:00 AM", "Mary Johnson", "📅 Upcoming"));

        LiveSessionAdapter adapter = new LiveSessionAdapter(sessions);
        rvLiveSessions.setAdapter(adapter);
    }

    private void loadTopFarmers() {
        List<TopFarmer> farmers = new ArrayList<>();
        farmers.add(new TopFarmer("🌽", "John's Farm", "4.9", "🌱 Organic Farmer"));
        farmers.add(new TopFarmer("🥔", "Green Acres", "4.8", "🥬 Vegetable Specialist"));
        farmers.add(new TopFarmer("🌾", "Golden Fields", "4.7", "🌾 Grain Expert"));
        farmers.add(new TopFarmer("🍅", "Sunrise Farm", "4.9", "🍅 Tomato Expert"));

        TopFarmerAdapter adapter = new TopFarmerAdapter(farmers);
        rvTopFarmers.setAdapter(adapter);
    }

    private void loadRecentQuestions() {
        List<RecentQuestion> questions = new ArrayList<>();
        questions.add(new RecentQuestion("What is the best season for planting maize?", "John Farmer", "2 hours ago"));
        questions.add(new RecentQuestion("How to control aphids naturally without chemicals?", "Sarah Green", "5 hours ago"));
        questions.add(new RecentQuestion("Best irrigation method for small farms under 2 acres?", "Dr. Wilson", "1 day ago"));
        questions.add(new RecentQuestion("What organic fertilizer gives the best yield for tomatoes?", "Mary Johnson", "2 days ago"));

        RecentQuestionAdapter adapter = new RecentQuestionAdapter(questions);
        rvRecentQuestions.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        Log.d(TAG, "setupBottomNavigation: Setting up bottom navigation");

        bottomNavigation.setSelectedItemId(R.id.nav_connect);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom nav item selected: " + itemId);

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, StudentDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_learning) {
                startActivity(new Intent(this, StudentLearningActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_marketplace) {
                startActivity(new Intent(this, StudentMarketplaceActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_connect) {
                startActivity(new Intent(this, StudentConnectActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, StudentProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void updateLastVisited() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastVisited = System.currentTimeMillis();
        prefs.edit().putLong("last_visited_connect", lastVisited).apply();
        Log.d(TAG, "updateLastVisited: " + lastVisited);
    }

    private void saveSessionDuration(long duration) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long totalTime = prefs.getLong("total_connect_time", 0);
        prefs.edit().putLong("total_connect_time", totalTime + duration).apply();
        Log.d(TAG, "Session duration: " + duration + "ms, Total: " + (totalTime + duration) + "ms");
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreSavedState: Restoring saved state");
    }

    private void checkAuthState() {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to login");
            navigateToLogin();
        } else {
            String userEmail = mAuth.getCurrentUser().getEmail();
            Log.d(TAG, "User logged in: " + userEmail);
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

    // ==================== MODEL CLASSES ====================

    static class LiveSession {
        String icon, title, time, host, status;
        LiveSession(String icon, String title, String time, String host, String status) {
            this.icon = icon;
            this.title = title;
            this.time = time;
            this.host = host;
            this.status = status;
        }
    }

    static class TopFarmer {
        String icon, name, rating, specialty;
        TopFarmer(String icon, String name, String rating, String specialty) {
            this.icon = icon;
            this.name = name;
            this.rating = rating;
            this.specialty = specialty;
        }
    }

    static class RecentQuestion {
        String question, farmer, time;
        RecentQuestion(String question, String farmer, String time) {
            this.question = question;
            this.farmer = farmer;
            this.time = time;
        }
    }

    // ==================== ADAPTERS ====================

    class LiveSessionAdapter extends RecyclerView.Adapter<LiveSessionAdapter.ViewHolder> {
        private List<LiveSession> sessions;

        LiveSessionAdapter(List<LiveSession> sessions) {
            this.sessions = sessions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_live_session, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LiveSession session = sessions.get(position);
            holder.tvIcon.setText(session.icon);
            holder.tvTitle.setText(session.title);
            holder.tvTime.setText(session.time);
            holder.tvHost.setText(session.host);
            holder.tvStatus.setText(session.status);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Join: " + session.title, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return sessions.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvTitle, tvTime, tvHost, tvStatus;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tvIcon);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvHost = itemView.findViewById(R.id.tvHost);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }

    class TopFarmerAdapter extends RecyclerView.Adapter<TopFarmerAdapter.ViewHolder> {
        private List<TopFarmer> farmers;

        TopFarmerAdapter(List<TopFarmer> farmers) {
            this.farmers = farmers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_top_farmer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TopFarmer farmer = farmers.get(position);
            holder.tvIcon.setText(farmer.icon);
            holder.tvName.setText(farmer.name);
            holder.tvRating.setText("⭐ " + farmer.rating);
            holder.tvSpecialty.setText(farmer.specialty);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "View profile: " + farmer.name, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return farmers.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvName, tvRating, tvSpecialty;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tvIcon);
                tvName = itemView.findViewById(R.id.tvName);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            }
        }
    }

    class RecentQuestionAdapter extends RecyclerView.Adapter<RecentQuestionAdapter.ViewHolder> {
        private List<RecentQuestion> questions;

        RecentQuestionAdapter(List<RecentQuestion> questions) {
            this.questions = questions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recent_question, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecentQuestion question = questions.get(position);
            holder.tvQuestion.setText(question.question);
            holder.tvFarmer.setText("👨‍🌾 " + question.farmer);
            holder.tvTime.setText(question.time);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "View question details", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return questions.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvQuestion, tvFarmer, tvTime;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvQuestion = itemView.findViewById(R.id.tvQuestion);
                tvFarmer = itemView.findViewById(R.id.tvFarmer);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}
