package org.matt.jiratomd.domain.model;

/**
 * Priority of an issue in the domain
 */
public enum Priority {
    HIGHEST("Highest"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    LOWEST("Lowest"),
    UNDEFINED("Undefined");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isCritical() {
        return this == HIGHEST || this == HIGH;
    }

    public static Priority fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNDEFINED;
        }

        String normalized = value.trim().toLowerCase();

        for (Priority priority : values()) {
            if (priority.name().toLowerCase().equals(normalized) ||
                    priority.label.toLowerCase().equals(normalized)) {
                return priority;
            }
        }

        // Support for common Jira formats
        if (normalized.contains("p1") || normalized.contains("highest")) {
            return HIGHEST;
        } else if (normalized.contains("p2") || normalized.contains("high")) {
            return HIGH;
        } else if (normalized.contains("p3") || normalized.contains("medium")) {
            return MEDIUM;
        } else if (normalized.contains("p4") || normalized.contains("low")) {
            return LOW;
        } else if (normalized.contains("p5") || normalized.contains("lowest")) {
            return LOWEST;
        }

        return UNDEFINED;
    }

    @Override
    public String toString() {
        return label;
    }
}