package org.matt.jiratomd.infrastructure.adapters;

import org.matt.jiratomd.domain.model.Issue;
import org.matt.jiratomd.domain.model.IssueFields;
import org.matt.jiratomd.domain.model.User;
import org.matt.jiratomd.domain.ports.input.IssueConverterPort;

import java.util.Objects;
import java.util.Optional;

/**
 * Adapter for converting issues to Markdown
 * Implements IssueConverterPort
 */
public class MarkdownConverterAdapter implements IssueConverterPort {

    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(MarkdownConverterAdapter.class);

    @Override
    public String convertToFormat(Issue issue, OutputFormat format) throws ConversionException {
        Objects.requireNonNull(issue, "Issue cannot be null");
        Objects.requireNonNull(format, "Format cannot be null");

        if (format != OutputFormat.MARKDOWN) {
            throw ConversionException.illegalFormat(issue.getKey(), format);
        }

        try {
            return convertIssueToMarkdown(issue);
        } catch (Exception e) {
            throw new ConversionException(
                    issue.getKey(),
                    format,
                    "Failed to convert issue to Markdown: " + e.getMessage(),
                    e
            );
        }
    }

    private String convertIssueToMarkdown(Issue issue) {
        IssueFields fields = issue.getFields();
        String issueKey = issue.getKey();

        logger.debug("Converting Jira issue {} to Markdown", issueKey);

        StringBuilder markdown = new StringBuilder();

        // Header with title
        markdown.append("# ").append(issueKey).append(" - ").append(fields.summary()).append("\n\n");

        // Metadata
        markdown.append("## Metadata\n\n");
        markdown.append("- **Statut:** ").append(fields.status().label()).append("\n");
        markdown.append("- **Type:** ").append(fields.issueType().name()).append("\n");
        markdown.append("- **Priorité:** ").append(fields.priority().label()).append("\n");

        // Dates
        markdown.append("- **Créé le:** ").append(fields.formatCreatedDate()).append("\n");
        markdown.append("- **Mis à jour le:** ").append(fields.formatUpdatedDate()).append("\n");

        // Users
        addUserInfo(markdown, "Créé par", fields.creator());
        addUserInfo(markdown, "Reported by", fields.reporter());
        addUserInfo(markdown, "Assigned to", fields.assignee());

        // Description
        if (fields.hasDescription()) {
            markdown.append("\n## Description\n\n");
            markdown.append(fields.description()).append("\n");
        } else {
            logger.debug("No description provided");
        }

        // Issue type (additional description)
        if (fields.issueType().hasDescription()) {
            markdown.append("\n## Issue Type\n\n");
            markdown.append(fields.issueType().description()).append("\n");
        } else {
            logger.debug("Issue type description included");
        }

        // Additional information
        markdown.append("\n## Additional Information\n\n");
        markdown.append("- **Key:** ").append(issueKey).append("\n");
        markdown.append("- **ID:** ").append(issue.getId().value()).append("\n");

        // Issue age
        Optional<java.time.Duration> age = issue.getAge();
        if (age.isPresent()) {
            long days = age.get().toDays();
            markdown.append("- **Age:** ").append(days).append(" day").append(days > 1 ? "s" : "").append("\n");
        }

        // Time since last update
        Optional<java.time.Duration> timeSinceUpdate = issue.getTimeSinceUpdate();
        if (timeSinceUpdate.isPresent()) {
            long hours = timeSinceUpdate.get().toHours();
            if (hours < 24) {
                markdown.append("- **Last update:** ").append(hours).append(" hour").append(hours > 1 ? "s" : "").append("\n");
            }
        }

        logger.info("Successfully converted Jira issue {} to Markdown", issueKey);
        return markdown.toString();
    }

    private void addUserInfo(StringBuilder markdown, String label, User user) {
        if (user != null) {
            markdown.append("- **").append(label).append(":** ");

            if (user.displayName() != null && !user.displayName().isBlank()) {
                markdown.append(user.displayName());
            }

            if (user.emailAddress() != null && !user.emailAddress().isBlank()) {
                markdown.append(" (").append(user.emailAddress()).append(")");
            }

            markdown.append("\n");

            if (logger.isDebugEnabled()) {
                String display = user.displayName() != null ? user.displayName() : "N/A";
                String email = user.emailAddress() != null ? user.emailAddress() : "N/A";
                logger.debug("Formatted user: {} ({})", display, email);
            }
        }
    }

    /**
     * Factory to create the adapter
     */
    public static MarkdownConverterAdapter create() {
        return new MarkdownConverterAdapter();
    }
}