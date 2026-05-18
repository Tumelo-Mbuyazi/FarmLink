package com.example.farmlink.models;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String category;
    private String instructor;
    private int duration;
    private int progress;
    private boolean completed;
    private int points;
    private boolean featured;
    private String imageUrl;
    private long timestamp;

    public Course() {} // Required for Firestore

    public Course(String title, String description, String category, String instructor) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructor = instructor;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
