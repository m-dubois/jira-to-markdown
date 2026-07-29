package org.matt.jiratomd.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Entity representing a Jira issue in the domain
 * - Identified by IssueId
 * - Contains business invariants
 * - Encapsulated business behavior
 */
public class Issue {

    private final IssueId id;
    private final String key; // Clé complète Jira (PROJ-123)
    private final IssueFields fields;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Private constructor, used via factory methods
    private Issue(IssueId id, String key, IssueFields fields,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        Objects.requireNonNull(id, "Issue ID cannot be null");
        Objects.requireNonNull(key, "Issue key cannot be null");
        Objects.requireNonNull(fields, "Issue fields cannot be null");

        this.id = id;
        this.key = key;
        this.fields = fields;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        // Invariant: the key must contain the correct ID
        validateKey();
    }

    private void validateKey() {
        if (!key.endsWith("-" + id.value())) {
            throw new IllegalArgumentException(
                    "Issue key '" + key + "' must end with ID '" + id.value() + "'");
        }
    }

    // Getters (no setters - immutable)
    public IssueId getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public IssueFields getFields() {
        return fields;
    }

    public Optional<LocalDateTime> getCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<LocalDateTime> getUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    // Business methods

    /**
     * Checks if the issue is resolved
     */
    public boolean isResolved() {
        return fields.status().isResolved();
    }

    @SuppressWarnings("unused")
    public boolean isInProgress() {
        return fields.status().isInProgress();
    }

    @SuppressWarnings("unused")
    public boolean isBlocked() {
        return fields.status().isBlocked();
    }

    /**
     * Checks if the issue is assigned to a specific user
     */
    public boolean isAssignedTo(User user) {
        return fields.isAssignedTo(user);
    }

    /**
     * Checks if the issue was created by a specific user
     */
    public boolean isCreatedBy(User user) {
        return fields.isCreatedBy(user);
    }

    @SuppressWarnings("unused")
    public boolean isCritical() {
        return fields.priority().isCritical();
    }

    @SuppressWarnings("unused")
    public boolean hasDescription() {
        return fields.hasDescription();
    }

    /**
     * Gets the elapsed time since creation
     */
    public Optional<java.time.Duration> getAge() {
        if (createdAt == null) {
            return Optional.empty();
        }
        return Optional.of(java.time.Duration.between(createdAt, LocalDateTime.now()));
    }

    /**
     * Gets the time since last update
     */
    public Optional<java.time.Duration> getTimeSinceUpdate() {
        if (updatedAt == null) {
            return Optional.empty();
        }
        return Optional.of(java.time.Duration.between(updatedAt, LocalDateTime.now()));
    }

    @SuppressWarnings("unused")
    public boolean isRecent(int maxDays) {
        return getAge()
                .map(age -> age.toDays() <= maxDays)
                .orElse(false);
    }

    /**
     * Validates business status transitions
     */
    @SuppressWarnings("unused")
    public boolean cannotTransitionTo(IssueStatus newStatus) {
        return !fields.status().canTransitionTo(newStatus);
    }

    @SuppressWarnings("unused")
    public Issue withStatus(IssueStatus newStatus) {
        if (cannotTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + fields.status() + " to " + newStatus);
        }

        IssueFields updatedFields = fields.toBuilder()
                .status(newStatus)
                .updated(LocalDateTime.now().toString())
                .build();

        return new Issue(id, key, updatedFields, createdAt, LocalDateTime.now());
    }

    @SuppressWarnings("unused")
    public Issue withAssignee(User newAssignee) {
        IssueFields updatedFields = fields.toBuilder()
                .assignee(newAssignee)
                .updated(LocalDateTime.now().toString())
                .build();

        return new Issue(id, key, updatedFields, createdAt, LocalDateTime.now());
    }

    // Factory methods

    @SuppressWarnings("unused")
    public static Issue create(String projectKey, IssueId id, IssueFields fields) {
        String fullKey = id.toFullKey(projectKey);
        LocalDateTime now = LocalDateTime.now();

        return new Issue(id, fullKey, fields, now, now);
    }

    public static Issue from(
            IssueId id,
            String key,
            IssueFields fields,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Issue(id, key, fields, createdAt, updatedAt);
    }

    // Equals/HashCode/ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return id.equals(issue.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Issue{key='" + key + "', summary='" + fields.summary() + "'}";
    }
}