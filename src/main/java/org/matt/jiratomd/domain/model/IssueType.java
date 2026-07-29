package org.matt.jiratomd.domain.model;

import java.util.Objects;

/**
 * Represents the type of an issue (Bug, Story, Task, etc.)
 * Value Object - Immutable
 */
public record IssueType(
        String name,
        String description
) {

    public IssueType {
        Objects.requireNonNull(name, "Issue type name cannot be null");
        // description can be null
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    @Override
    public String toString() {
        return name;
    }

    public static IssueType bug() {
        return new IssueType("Bug", "A bug or defect");
    }

    public static IssueType story() {
        return new IssueType("Story", "User story");
    }

    public static IssueType task() {
        return new IssueType("Task", "A task that needs to be done");
    }

    public static IssueType fromString(String value) {
        if (value == null || value.isBlank()) {
            return new IssueType("Task", "A task that needs to be done");
        }

        String lowerValue = value.trim().toLowerCase();

        // Simple mappings
        if (lowerValue.contains("bug") || lowerValue.contains("defect")) {
            return bug();
        } else if (lowerValue.contains("story") || lowerValue.contains("user story")) {
            return story();
        } else if (lowerValue.contains("task")) {
            return task();
        } else if (lowerValue.contains("epic")) {
            return new IssueType("Epic", "A large body of work");
        } else if (lowerValue.contains("improvement") || lowerValue.contains("enhancement")) {
            return new IssueType("Improvement", "An improvement to existing functionality");
        } else if (lowerValue.contains("feature") || lowerValue.contains("new feature")) {
            return new IssueType("Feature", "New functionality");
        } else if (lowerValue.contains("spike") || lowerValue.contains("research")) {
            return new IssueType("Spike", "Technical research or investigation");
        }

        // Otherwise create a new type with the given name
        return new IssueType(value, null);
    }
}