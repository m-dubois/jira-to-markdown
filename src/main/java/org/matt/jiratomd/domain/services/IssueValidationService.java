package org.matt.jiratomd.domain.services;

import org.matt.jiratomd.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business validation service for issues
 * Contains domain-specific validation rules
 */
public class IssueValidationService {

    /**
     * Validates that an issue can be created with the given parameters
     */
    @SuppressWarnings("unused")
    public ValidationResult validateForCreation(
            IssueId id,
            String projectKey,
            IssueFields fields
    ) {
        ValidationResult result = new ValidationResult();

        // 1. Check the identifier
        if (id == null) {
            result.addError("Issue ID cannot be null");
        } else if (id.value() <= 0) {
            result.addError("Issue ID must be positive");
        }

        // 2. Check the project key
        if (projectKey == null || projectKey.isBlank()) {
            result.addError("Project key cannot be null or blank");
        } else if (!projectKey.matches("^[A-Z][A-Z0-9_]*$")) {
            result.addError("Project key must contain only uppercase letters, numbers and underscores");
        }

        // 3. Check the fields
        validateFields(fields, result);

        // 4. Check consistency between the ID and the project
        if (id != null && projectKey != null) {
            String expectedKey = projectKey + "-" + id.value();
            result.setExpectedIssueKey(expectedKey);
        }

        return result;
    }

    /**
     * Validates an existing issue
     */
    public ValidationResult validateExistingIssue(Issue issue) {
        ValidationResult result = new ValidationResult();

        if (issue == null) {
            result.addError("Issue cannot be null");
            return result;
        }

        // Check key consistency
        String fullKey = issue.getKey();
        String[] parts = fullKey.split("-");
        if (parts.length != 2) {
            result.addError("Issue key must follow format PROJECT-ID");
        } else {
            result.setExpectedIssueKey(fullKey);
        }

        // Check the fields
        validateFields(issue.getFields(), result);

        // Check dates (business logic)
        Optional<LocalDateTime> createdAt = issue.getCreatedAt();
        Optional<LocalDateTime> updatedAt = issue.getUpdatedAt();

        if (createdAt.isPresent() && updatedAt.isPresent()) {
            if (updatedAt.get().isBefore(createdAt.get())) {
                result.addError("Update date cannot be before creation date");
            }
        }

        return result;
    }

    /**
     * Validates that a user can be assigned to an issue
     */
    @SuppressWarnings("unused")
    public ValidationResult validateAssignment(Issue issue, User user) {
        ValidationResult result = new ValidationResult();

        if (issue == null) {
            result.addError("Issue cannot be null");
            return result;
        }

        if (user == null) {
            result.addError("User cannot be null");
            return result;
        }

        // Business rules:
        // 1. A resolved issue cannot be reassigned
        if (issue.isResolved()) {
            result.addError("Cannot assign user to resolved issue");
        }

        // 2. Check that the user is not already assigned
        if (issue.isAssignedTo(user)) {
            result.addError("User is already assigned to this issue");
        }

        // 3. The assignee cannot be the creator (optional rule)
        if (issue.isCreatedBy(user)) {
            result.addError("Creator cannot be assigned to their own issue");
        }

        return result;
    }

    /**
     * Validates a status transition
     */
    @SuppressWarnings("unused")
    public ValidationResult validateStatusTransition(
            Issue issue,
            IssueStatus newStatus
    ) {
        ValidationResult result = new ValidationResult();

        if (issue == null) {
            result.addError("Issue cannot be null");
            return result;
        }

        if (newStatus == null) {
            result.addError("New status cannot be null");
            return result;
        }

        IssueStatus currentStatus = issue.getFields().status();

        // Business rules:
        // 1. Check that the transition is allowed
        if (issue.cannotTransitionTo(newStatus)) {
            result.addError(String.format(
                    "Cannot transition from %s to %s",
                    currentStatus, newStatus
            ));
        }

        // 2. An assigned issue cannot move to TODO
        if (newStatus == IssueStatus.TODO && issue.getFields().isAssigned()) {
            result.addError("Cannot move assigned issue back to TODO");
        }

        // 3. A BLOCKED issue must have a reason (optional field)
        if (newStatus == IssueStatus.BLOCKED) {
            if (issue.getFields().description() == null || issue.getFields().description().isBlank()) {
                result.addWarning("Blocked issues should have a description explaining the block");
            }
        }

        return result;
    }

    private void validateFields(IssueFields fields, ValidationResult result) {
        if (fields == null) {
            result.addError("Issue fields cannot be null");
            return;
        }

        // 1. Check the summary (required)
        if (fields.summary() == null || fields.summary().isBlank()) {
            result.addError("Issue summary cannot be null or blank");
        } else if (fields.summary().length() > 255) {
            result.addError("Issue summary cannot exceed 255 characters");
        }

        // 2. Check the description (optional but limited)
        if (fields.description() != null && fields.description().length() > 10000) {
            result.addError("Issue description cannot exceed 10000 characters");
        }

        // 3. Check the issue type
        if (fields.issueType() == null) {
            result.addError("Issue type cannot be null");
        }

        // 4. Check the status
        if (fields.status() == null) {
            result.addError("Issue status cannot be null");
        }

        // 5. Check the priority
        if (fields.priority() == null) {
            result.addError("Issue priority cannot be null");
        }

        // 6. Check creation/update dates (format)
        try {
            if (fields.created() != null) {
                fields.createdDateTime().orElseThrow(() ->
                        new IllegalArgumentException("Invalid creation date format")
                );
            }

            if (fields.updated() != null) {
                fields.updatedDateTime().orElseThrow(() ->
                        new IllegalArgumentException("Invalid update date format")
                );
            }
        } catch (IllegalArgumentException e) {
            result.addError(e.getMessage());
        }
    }

    /**
     * Validation result containing errors and warnings
     */
    public static class ValidationResult {
        private final List<String> errors = new java.util.ArrayList<>();
        private final List<String> warnings = new java.util.ArrayList<>();
        // expectedIssueKey has been removed because it was not used - can be reintroduced if necessary

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        @SuppressWarnings("unused")
        public void setExpectedIssueKey(String key) {
            // For future feature: expected key validation
            // This method can be implemented if necessary
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<String> getWarnings() {
            return List.copyOf(warnings);
        }

        public void throwIfInvalid() {
            if (!isValid()) {
                throw new ValidationException(
                        "Issue validation failed: " + String.join(", ", errors)
                );
            }
        }
    }

    /**
     * Business validation exception
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }

        @SuppressWarnings("unused")
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}