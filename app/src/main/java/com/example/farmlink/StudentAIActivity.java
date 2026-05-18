package com.example.farmlink;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StudentAIActivity extends AppCompatActivity {

    // TODO: Replace with your actual Gemini API Key
    private static final String API_KEY = "AIzaSyBliCpvhBW6cJO1hcKEn3rGd2I1Cxe5jO8";

    private EditText etMessage;
    private ImageButton btnSend;
    private LinearLayout chatContainer;
    private ScrollView scrollView;

    private GenerativeModelFutures model;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler();
    private boolean isWaitingForResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_ai);

        initViews();
        setupGemini();
        setupClickListeners();
        addWelcomeMessage();
    }

    private void initViews() {
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupGemini() {
        try {
            // ✅ CORRECT MODEL NAME - Try one of these:
            // "models/gemini-1.5-flash" - Faster, lighter
            // "models/gemini-1.5-pro" - More capable, slightly slower
            GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
            model = GenerativeModelFutures.from(gm);

        } catch (Exception e) {
            addBotMessage("⚠️ API Key error: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void addWelcomeMessage() {
        String welcomeMsg = "🌾 **Hello! I'm FarmLink AI Assistant**\n\n here to help with:\n\n• 🌽 Crop farming techniques\n• 🥬 Vegetable growing guides\n• 🐄 Livestock care\n• 🐛 Pest control solutions\n• 🌱 Soil health management\n\n**Ask me anything about agriculture!**";
      
    }

    private void sendMessage() {
        if (isWaitingForResponse) {
            Toast.makeText(this, "Please wait for response...", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            return;
        }

        addUserMessage(message);
        etMessage.setText("");
        getGeminiResponse(message);
    }

    private void getGeminiResponse(String userMessage) {
        if (model == null) {
            addBotMessage("⚠️ AI is not configured. Please check your API key.");
            return;
        }

        isWaitingForResponse = true;
        showTypingIndicator();

        String prompt = "You are FarmLink AI, an expert agricultural assistant. " +
                "Answer this farming question concisely and helpfully (max 150 words):\n\n" +
                "Question: " + userMessage + "\n\n" +
                "Answer:";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                mainHandler.post(() -> {
                    hideTypingIndicator();
                    String aiResponse = result.getText();
                    addBotMessage(aiResponse);
                    isWaitingForResponse = false;
                });
            }

            @Override
            public void onFailure(Throwable t) {
                mainHandler.post(() -> {
                    hideTypingIndicator();
                    String errorMsg = t.getMessage();

                    if (errorMsg != null && errorMsg.contains("API key")) {
                        addBotMessage("⚠️ **Invalid API Key**\n\nGet a free key at: https://aistudio.google.com/app/apikey");
                    } else if (errorMsg != null && errorMsg.contains("404")) {
                        addBotMessage("⚠️ **Model Error**\n\nTry using: 'models/gemini-1.5-flash' or 'models/gemini-1.5-pro'");
                    } else if (errorMsg != null && errorMsg.contains("quota")) {
                        addBotMessage("⚠️ **Rate Limit Exceeded**\n\nFree tier limit reached. Try again later.");
                    } else {
                        addBotMessage("⚠️ **Error**: " + errorMsg);
                    }
                    isWaitingForResponse = false;
                });
            }
        }, executor);
    }

    private void showTypingIndicator() {
        LinearLayout botLayout = new LinearLayout(this);
        botLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        botLayout.setOrientation(LinearLayout.HORIZONTAL);
        botLayout.setGravity(android.view.Gravity.START);
        botLayout.setPadding(0, 0, 0, 16);

        TextView icon = new TextView(this);
        icon.setText("🤖");
        icon.setTextSize(20);
        icon.setBackgroundResource(R.drawable.circle_bg);
        icon.setPadding(10, 10, 10, 10);

        CardView cardView = new CardView(this);
        cardView.setRadius(16f);
        cardView.setCardBackgroundColor(getColor(android.R.color.white));

        TextView textView = new TextView(this);
        textView.setText("...");
        textView.setTextColor(getColor(android.R.color.black));
        textView.setPadding(32, 16, 32, 16);

        cardView.addView(textView);
        botLayout.addView(icon);
        botLayout.addView(cardView);
        chatContainer.addView(botLayout);
        scrollToBottom();
    }

    private void hideTypingIndicator() {
        int childCount = chatContainer.getChildCount();
        if (childCount > 0) {
            View lastView = chatContainer.getChildAt(childCount - 1);
            if (lastView instanceof LinearLayout) {
                LinearLayout lastLayout = (LinearLayout) lastView;
                if (lastLayout.getChildCount() > 1) {
                    View cardView = lastLayout.getChildAt(1);
                    if (cardView instanceof CardView) {
                        CardView card = (CardView) cardView;
                        if (card.getChildAt(0) instanceof TextView) {
                            TextView text = (TextView) card.getChildAt(0);
                            if (text.getText().equals("...")) {
                                chatContainer.removeViewAt(childCount - 1);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addUserMessage(String message) {
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setGravity(android.view.Gravity.END);
        userLayout.setPadding(0, 0, 0, 16);

        CardView cardView = new CardView(this);
        cardView.setRadius(16f);
        cardView.setCardBackgroundColor(getColor(R.color.farmer_green));

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getColor(android.R.color.white));
        textView.setPadding(32, 16, 32, 16);

        cardView.addView(textView);
        userLayout.addView(cardView);
        chatContainer.addView(userLayout);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        LinearLayout botLayout = new LinearLayout(this);
        botLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        botLayout.setOrientation(LinearLayout.HORIZONTAL);
        botLayout.setGravity(android.view.Gravity.START);
        botLayout.setPadding(0, 0, 0, 16);

        TextView icon = new TextView(this);
        icon.setText("🤖");
        icon.setTextSize(20);
        icon.setBackgroundResource(R.drawable.circle_bg);
        icon.setPadding(10, 10, 10, 10);

        CardView cardView = new CardView(this);
        cardView.setRadius(16f);
        cardView.setCardBackgroundColor(getColor(android.R.color.white));

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getColor(android.R.color.black));
        textView.setPadding(32, 16, 32, 16);

        cardView.addView(textView);
        botLayout.addView(icon);
        botLayout.addView(cardView);
        chatContainer.addView(botLayout);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
}