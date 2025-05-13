package com.example.a404.data.model;

public class Achievement {
    private String id; // ID dokumentu z Firestore, ustawiane po pobraniu
    private String name;
    private String description;
    private String iconName; // Nazwa zasobu drawable dla ikony (np. "ic_achievement_first_step")
    private String triggerType; // Typ zdarzenia, np. "LESSONS_COMPLETED", "LANGUAGES_STARTED"
    private Object triggerValue; // Wartość potrzebna do odblokowania (np. liczba 1, 2 lub string)
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



    // Gettery
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public String getTriggerType() { return triggerType; }
    public Object getTriggerValue() { return triggerValue; }

    // Settery (głównie dla Firestore i ustawiania ID po pobraniu)
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public void setTriggerValue(Object triggerValue) { this.triggerValue = triggerValue; }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }
}