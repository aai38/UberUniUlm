package de.uni_ulm.uberuniulm.model;

public class Settings {
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    private String language;
    private String theme;

    public Settings(String language, String theme) {
        this.language = language;
        this.theme = theme;
    }
}
