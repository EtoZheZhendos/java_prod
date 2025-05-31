package com.studentbudget.model;

public enum UserRole {
    STUDENT("Студент"),
    PARENT("Родитель"),
    ADMIN("Администратор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 