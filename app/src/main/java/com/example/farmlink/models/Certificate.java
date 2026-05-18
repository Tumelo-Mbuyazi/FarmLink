package com.example.farmlink.models;

public class Certificate {
    private String certificateId;
    private String studentId;
    private String courseId;
    private String courseName;
    private long earnedDate;
    private int points;
    private String pdfUrl;

    public Certificate() {
        // Required for Firestore
    }

    public Certificate(String studentId, String courseId, String courseName, long earnedDate, int points) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.earnedDate = earnedDate;
        this.points = points;
    }

    // Getters and Setters
    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public long getEarnedDate() {
        return earnedDate;
    }

    public void setEarnedDate(long earnedDate) {
        this.earnedDate = earnedDate;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
