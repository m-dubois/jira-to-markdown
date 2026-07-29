package org.matt.jiratomd.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * Fields of a Jira issue
 * Value Object - Immutable
 */
public record IssueFields(
        String summary,
        String description,
        IssueType issueType,
        User assignee,
        User creator,
        User reporter,
        String created,
        String updated,
        IssueStatus status,
        Priority priority
) {

    public IssueFields {
        Objects.requireNonNull(summary, "Summary cannot be null");
        Objects.requireNonNull(issueType, "Issue type cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(priority, "Priority cannot be null");

        // Assignee, creator, reporter can be null
        // created and updated can be null (formatted later)
        // description can be null or empty
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean isAssigned() {
        return assignee != null;
    }

    public boolean isAssignedTo(User user) {
        if (assignee == null || user == null) {
            return false;
        }
        return assignee.isSameUser(user);
    }

    public boolean isCreatedBy(User user) {
        if (creator == null || user == null) {
            return false;
        }
        return creator.isSameUser(user);
    }

    public Optional<LocalDateTime> createdDateTime() {
        return parseDateTime(created);
    }

    public Optional<LocalDateTime> updatedDateTime() {
        return parseDateTime(updated);
    }

    private Optional<LocalDateTime> parseDateTime(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return Optional.empty();
        }

        try {
            // Jira ISO format: "2026-06-09T14:24:39.845+0000"
            // We take the first 19 characters to ignore milliseconds and timezone
            String isoPart = dateString.substring(0, Math.min(dateString.length(), 19));
            LocalDateTime dateTime = LocalDateTime.parse(isoPart);
            return Optional.of(dateTime);
        } catch (DateTimeParseException | IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public String formatCreatedDate() {
        return formatDate(created);
    }

    public String formatUpdatedDate() {
        return formatDate(updated);
    }

    private String formatDate(String dateString) {
        return parseDateTime(dateString)
                .map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .orElse("Unknown date");
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for IssueFields (because record, need mutability for construction)
     */
    public static class Builder {
        private String summary = "";
        private String description = "";
        private IssueType issueType = IssueType.task();
        private User assignee = null;
        private User creator = User.anonymous();
        private User reporter = User.anonymous();
        private String created = null;
        private String updated = null;
        private IssueStatus status = IssueStatus.TODO;
        private Priority priority = Priority.MEDIUM;

        public Builder() {
        }

        public Builder(IssueFields source) {
            this.summary = source.summary;
            this.description = source.description;
            this.issueType = source.issueType;
            this.assignee = source.assignee;
            this.creator = source.creator;
            this.reporter = source.reporter;
            this.created = source.created;
            this.updated = source.updated;
            this.status = source.status;
            this.priority = source.priority;
        }

        public Builder summary(String summary) {
            this.summary = Objects.requireNonNull(summary, "Summary cannot be null");
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder issueType(IssueType issueType) {
            this.issueType = Objects.requireNonNull(issueType, "Issue type cannot be null");
            return this;
        }

        public Builder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder creator(User creator) {
            this.creator = Objects.requireNonNull(creator, "Creator cannot be null");
            return this;
        }

        public Builder reporter(User reporter) {
            this.reporter = Objects.requireNonNull(reporter, "Reporter cannot be null");
            return this;
        }

        public Builder created(String created) {
            this.created = created;
            return this;
        }

        public Builder updated(String updated) {
            this.updated = updated;
            return this;
        }

        public Builder status(IssueStatus status) {
            this.status = Objects.requireNonNull(status, "Status cannot be null");
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
            return this;
        }

        public IssueFields build() {
            return new IssueFields(
                    summary,
                    description,
                    issueType,
                    assignee,
                    creator,
                    reporter,
                    created,
                    updated,
                    status,
                    priority
            );
        }
    }
}