package com.studentbudget.service;

import java.util.Locale;

public interface SettingsService {
    void setLanguage(Locale locale);
    Locale getCurrentLanguage();
    void setTheme(String theme);
    String getCurrentTheme();
    void setCurrency(String currencyCode);
    String getCurrentCurrency();
    void saveSettings();
    void loadSettings();
} 