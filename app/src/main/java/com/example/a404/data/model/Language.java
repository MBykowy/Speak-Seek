// Ścieżka: app/java/com/example/a404/data/model/Language.java
package com.example.a404.data.model;

public class Language {
    private String code;
    private String name;
    private int flagResourceId; // <<< NOWE POLE

    // Pusty konstruktor (jeśli jest potrzebny, np. dla Firebase)
    public Language() {}

    // Główny konstruktor używany w ViewModel
    public Language(String code, String name, int flagResourceId) {
        this.code = code;
        this.name = name;
        this.flagResourceId = flagResourceId;
    }

    // Konstruktor bez flagi (jeśli potrzebny dla wstecznej kompatybilności, ustawia flagę na 0)
    public Language(String code, String name) {
        this(code, name, 0); // Wywołuje główny konstruktor z flagResourceId = 0
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFlagResourceId() { // <<< NOWY GETTER
        return flagResourceId;
    }

    public void setFlagResourceId(int flagResourceId) { // <<< NOWY SETTER (opcjonalny)
        this.flagResourceId = flagResourceId;
    }
}