package org.matt.jiratomd.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.matt.jiratomd.domain.model.*;

import java.time.LocalDateTime;

/**
 * DTO for JSON serialization/deserialization of a Jira issue
 * Simplified version for Jackson with records
 */
@SuppressWarnings("unused")
public record IssueDto(
        @JsonProperty("id") String id,
        @JsonProperty("key") String key,
        @JsonProperty("fields") FieldsDto fields
) {

    @JsonCreator
    public IssueDto {
        // Simple validation
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Issue key cannot be null or blank");
        }
    }

    /**
     * Converts DTO to domain entity
     */
    public Issue toDomain() {
        // Extract ID from the key (ex: PROJ-123 -> IssueId(123))
        String[] parts = key().split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid issue key format: " + key());
        }

        IssueId issueId = new IssueId(parts[1]);

        // Extract fields from the DTO
        IssueFields issueFields = fields().toDomain();

        // Use the public factory method
        LocalDateTime createdAt = issueFields.createdDateTime().orElse(null);
        LocalDateTime updatedAt = issueFields.updatedDateTime().orElse(null);

        return Issue.from(issueId, key(), issueFields, createdAt, updatedAt);
    }

    /**
     * DTO for issue fields
     */
    public record FieldsDto(
            @JsonProperty("summary") String summary,
            @JsonProperty("description") String description,
            @JsonProperty("issuetype") IssueTypeDto issueType,
            @JsonProperty("assignee") UserDto assignee,
            @JsonProperty("creator") UserDto creator,
            @JsonProperty("reporter") UserDto reporter,
            @JsonProperty("created") String created,
            @JsonProperty("updated") String updated,
            @JsonProperty("status") StatusDto status,
            @JsonProperty("priority") PriorityDto priority
    ) {

        @JsonCreator
        public FieldsDto {
        }

        public IssueFields toDomain() {
            return IssueFields.builder()
                    .summary(summary())
                    .description(description())
                    .issueType(issueType().toDomain())
                    .assignee(assignee() != null ? assignee().toDomain() : null)
                    .creator(creator() != null ? creator().toDomain() : User.anonymous())
                    .reporter(reporter() != null ? reporter().toDomain() : User.anonymous())
                    .created(created())
                    .updated(updated())
                    .status(status().toDomain())
                    .priority(priority().toDomain())
                    .build();
        }
    }

    /**
     * DTO for issue type
     */
    public record IssueTypeDto(
            @JsonProperty("name") String name
    ) {

        @JsonCreator
        public IssueTypeDto {
        }

        public IssueType toDomain() {
            return IssueType.fromString(name());
        }
    }

    /**
     * DTO for user
     */
    public record UserDto(
            @JsonProperty("accountId") String accountId,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("emailAddress") String emailAddress
    ) {

        @JsonCreator
        public UserDto {
        }

        public User toDomain() {
            return new User(accountId(), displayName(), emailAddress());
        }
    }

    /**
     * DTO for status
     */
    public record StatusDto(
            @JsonProperty("name") String name
    ) {

        @JsonCreator
        public StatusDto {
        }

        public IssueStatus toDomain() {
            return IssueStatus.fromString(name());
        }
    }

    /**
     * DTO for priority
     */
    public record PriorityDto(
            @JsonProperty("name") String name
    ) {

        @JsonCreator
        public PriorityDto {
        }

        public Priority toDomain() {
            return Priority.fromString(name());
        }
    }

}