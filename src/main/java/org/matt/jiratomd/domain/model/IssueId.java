package org.matt.jiratomd.domain.model;

import java.util.Objects;

/**
 * Unique numeric identifier of a Jira issue
 * Value Object - Immutable
 */
public record IssueId(int value) {

    public IssueId {
        if (value <= 0) {
            throw new IllegalArgumentException("Issue ID value must be positive: " + value);
        }
    }

    public IssueId(String stringValue) {
        this(Integer.parseInt(stringValue.trim()));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Factory method to create an IssueId from a complete Jira key
     * Example: "PROJ1-12714" -> 12714
     */
    public static IssueId fromFullKey(String fullKey) {
        Objects.requireNonNull(fullKey, "Full key cannot be null");

        if (!fullKey.contains("-")) {
            throw new IllegalArgumentException("Invalid Jira key format: " + fullKey);
        }

        String idPart = fullKey.split("-")[1];
        try {
            return new IssueId(Integer.parseInt(idPart.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid issue ID in key '" + fullKey + "': " + idPart, e);
        }
    }

    /**
     * Creates the complete Jira key with project prefix
     */
    public String toFullKey(String projectKey) {
        Objects.requireNonNull(projectKey, "Project key cannot be null");
        return projectKey + "-" + value;
    }
}