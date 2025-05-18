package com.studentbudget.service.impl;

import com.studentbudget.service.SettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SettingsServiceImpl implements SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsServiceImpl.class);
    private static final String SETTINGS_FILE = "settings.json";
    private final ObjectMapper objectMapper;
    private Map<String, Object> settings;

    public SettingsServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.settings = new HashMap<>();
        loadSettings();
    }

    @Override
    public void setLanguage(Locale locale) {
        logger.debug("Setting language to: {}", locale);
        settings.put("language", locale.toString());
        saveSettings();
    }

    @Override
    public Locale getCurrentLanguage() {
        String language = (String) settings.getOrDefault("language", "ru_RU");
        return Locale.forLanguageTag(language.replace('_', '-'));
    }

    @Override
    public void setTheme(String theme) {
        logger.debug("Setting theme to: {}", theme);
        settings.put("theme", theme);
        saveSettings();
    }

    @Override
    public String getCurrentTheme() {
        return (String) settings.getOrDefault("theme", "dark");
    }

    @Override
    public void setCurrency(String currencyCode) {
        logger.debug("Setting currency to: {}", currencyCode);
        settings.put("currency", currencyCode);
        saveSettings();
    }

    @Override
    public String getCurrentCurrency() {
        return (String) settings.getOrDefault("currency", "RUB");
    }

    @Override
    public void saveSettings() {
        try {
            objectMapper.writeValue(new File(SETTINGS_FILE), settings);
            logger.debug("Settings saved successfully");
        } catch (IOException e) {
            logger.error("Failed to save settings", e);
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    @Override
    public void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);
        if (settingsFile.exists()) {
            try {
                settings = objectMapper.readValue(settingsFile, HashMap.class);
                logger.debug("Settings loaded successfully");
            } catch (IOException e) {
                logger.error("Failed to load settings", e);
                settings = new HashMap<>();
            }
        } else {
            logger.debug("Settings file not found, using defaults");
            // Set default settings
            settings.put("language", "ru_RU");
            settings.put("theme", "dark");
            settings.put("currency", "RUB");
            saveSettings();
        }
    }
} 