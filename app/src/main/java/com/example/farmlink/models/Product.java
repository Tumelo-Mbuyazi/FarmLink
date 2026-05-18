package com.example.farmlink.models;

public class Product {
    private String productId;
    private String farmerId;
    private String farmerName;
    private String name;
    private double price;
    private String unit;
    private int quantity;
    private String category;
    private String description;
    private boolean isOrganic;
    private String status;
    private double averageRating;
    private long timestamp;
    private String emoji;  // NEW: emoji field

    // Required empty constructor for Firestore
    public Product() {}

    // Comprehensive constructor (UPDATED with emoji parameter)
    public Product(String productId, String farmerId, String farmerName, String name, double price, String unit,
                   int quantity, String category, String description, boolean isOrganic,
                   String status, double averageRating, long timestamp, String emoji) {
        this.productId = productId;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
        this.isOrganic = isOrganic;
        this.status = status;
        this.averageRating = averageRating;
        this.timestamp = timestamp;
        this.emoji = emoji;
    }

    // NEW: Get emoji - auto-generates if not set
    public String getEmoji() {
        if (emoji != null && !emoji.isEmpty()) {
            return emoji;
        }
        return getEmojiForProduct(this.name);
    }

    // NEW: Set emoji manually
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    // NEW: Helper method to map product name to emoji
    private String getEmojiForProduct(String productName) {
        if (productName == null) return "🌾";

        switch (productName.toLowerCase()) {
            case "maize":
            case "corn":
                return "🌽";
            case "potato":
            case "potatoes":
                return "🥔";
            case "wheat":
                return "🌾";
            case "soybean":
            case "soybeans":
                return "🌱";
            case "tomato":
            case "tomatoes":
                return "🍅";
            case "rice":
                return "🍚";
            case "carrot":
            case "carrots":
                return "🥕";
            case "onion":
            case "onions":
                return "🧅";
            case "cabbage":
                return "🥬";
            case "apple":
            case "apples":
                return "🍎";
            case "orange":
            case "oranges":
                return "🍊";
            default:
                return "🥬";
        }
    }

    // Existing Getters and Setters (unchanged)
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    private double distance;  // Add this field

    // Getter and Setter
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public boolean isOrganic() { return isOrganic; }
    public void setOrganic(boolean organic) { isOrganic = organic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}