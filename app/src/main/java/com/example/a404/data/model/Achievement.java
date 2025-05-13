package com.example.a404.data.model;

public class Achievement {
    private String id;
    private String name;
    private String description;
    private int pointsAwarded;
    private String iconUrl;

    // Wymagany pusty konstruktor dla Firestore
    public Achievement() {
    }

    public Achievement(String id, String name, String description, int pointsAwarded) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsAwarded = pointsAwarded;
    }

    public Achievement(String id, String name, String description, int pointsAwarded, String iconUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsAwarded = pointsAwarded;
        this.iconUrl = iconUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}