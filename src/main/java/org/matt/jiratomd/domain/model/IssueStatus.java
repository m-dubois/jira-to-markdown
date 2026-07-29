package org.matt.jiratomd.domain.model;

/**
 * Status of an issue in the domain
 */
public enum IssueStatus {
    TODO("To Do", false),
    IN_PROGRESS("In Progress", false),
    IN_REVIEW("In Review", false),
    DONE("Done", true),
    CANCELLED("Cancelled", true),
    BLOCKED("Blocked", false),
    UNDEFINED("Undefined", false);

    private final String label;
    private final boolean resolved;

    IssueStatus(String label, boolean resolved) {
        this.label = label;
        this.resolved = resolved;
    }

    public String label() {
        return label;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS || this == IN_REVIEW;
    }

    public boolean isBlocked() {
        return this == BLOCKED;
    }

    public boolean canTransitionTo(IssueStatus target) {
        // Simple transition rules
        if (this == target) {
            return true; // Can stay in the same state
        }

        if (this.isResolved()) {
            // A resolved issue cannot return to an unresolved state
            return target.isResolved();
        }

        // Allowed states from each state
        return switch (this) {
            case TODO -> target == IN_PROGRESS || target == BLOCKED || target == CANCELLED;
            case IN_PROGRESS -> target == IN_REVIEW || target == DONE || target == BLOCKED;
            case IN_REVIEW -> target == DONE || target == IN_PROGRESS;
            case BLOCKED -> target == TODO || target == IN_PROGRESS;
            default -> false;
        };
    }

    public static IssueStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNDEFINED;
        }

        String normalized = value.trim().toLowerCase();

        for (IssueStatus status : values()) {
            if (status.name().toLowerCase().replace("_", " ").contains(normalized) ||
                    status.label.toLowerCase().contains(normalized)) {
                return status;
            }
        }

        // Common Jira -> Domain mappings
        if (normalized.contains("to do") || normalized.contains("open")) {
            return TODO;
        } else if (normalized.contains("in progress") || normalized.contains("progress")) {
            return IN_PROGRESS;
        } else if (normalized.contains("review")) {
            return IN_REVIEW;
        } else if (normalized.contains("done") || normalized.contains("closed") || normalized.contains("resolved")) {
            return DONE;
        } else if (normalized.contains("cancel")) {
            return CANCELLED;
        } else if (normalized.contains("block")) {
            return BLOCKED;
        }

        return UNDEFINED;
    }

    @Override
    public String toString() {
        return label;
    }
}