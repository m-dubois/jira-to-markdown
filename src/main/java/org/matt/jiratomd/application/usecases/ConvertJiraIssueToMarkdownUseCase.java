package org.matt.jiratomd.application.usecases;

import org.matt.jiratomd.domain.ports.input.IssueConverterPort;
import org.matt.jiratomd.domain.ports.output.ContentWriterPort;
import org.matt.jiratomd.domain.services.IssueValidationService;
import org.matt.jiratomd.infrastructure.adapters.JiraApiAdapter;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Specific use case for converting Jira issues to Markdown
 * Uses JiraApiAdapter directly to support fetchIssueByKey
 */
public class ConvertJiraIssueToMarkdownUseCase {

    private final JiraApiAdapter jiraApiAdapter;
    private final IssueConverterPort issueConverter;
    private final ContentWriterPort contentWriter;
    private final IssueValidationService validationService;

    public ConvertJiraIssueToMarkdownUseCase(
            JiraApiAdapter jiraApiAdapter,
            IssueConverterPort issueConverter,
            ContentWriterPort contentWriter,
            IssueValidationService validationService
    ) {
        this.jiraApiAdapter = Objects.requireNonNull(jiraApiAdapter, "JiraApiAdapter cannot be null");
        this.issueConverter = Objects.requireNonNull(issueConverter, "IssueConverterPort cannot be null");
        this.contentWriter = Objects.requireNonNull(contentWriter, "ContentWriterPort cannot be null");
        this.validationService = Objects.requireNonNull(validationService, "ValidationService cannot be null");
    }

    /**
     * Converts a Jira issue to Markdown and writes the result
     *
     * @param fullIssueKey    The complete issue key (ex: "PROJ-123")
     * @param outputDirectory Output directory for the Markdown file
     * @param outputFormat    Output format (optional, MARKDOWN by default)
     * @return The complete path of the generated file
     * @throws ConversionException If the conversion fails
     */
    public Path convertIssue(
            String fullIssueKey,
            Path outputDirectory,
            IssueConverterPort.OutputFormat outputFormat
    ) throws ConversionException {
        Objects.requireNonNull(fullIssueKey, "Issue key cannot be null");
        Objects.requireNonNull(outputDirectory, "Output directory cannot be null");

        if (outputFormat == null) {
            outputFormat = IssueConverterPort.OutputFormat.MARKDOWN;
        }

        // Extract the ID from the complete key for validation
        String[] parts = fullIssueKey.split("-");
        if (parts.length != 2) {
            throw new ConversionException(
                    "Invalid issue key format. Expected PROJECT-ID, got: " + fullIssueKey,
                    fullIssueKey
            );
        }

        try {
            // 1. Retrieve the issue from Jira with the complete key
            var issue = jiraApiAdapter.fetchIssueByKey(fullIssueKey)
                    .orElseThrow(() -> new ConversionException(
                            "Issue not found: " + fullIssueKey,
                            fullIssueKey
                    ));

            // 2. Validate the issue
            var validation = validationService.validateExistingIssue(issue);
            validation.throwIfInvalid();

            if (validation.hasWarnings()) {
                System.err.println("Warning for issue " + issue.getKey() + ":");
                validation.getWarnings().forEach(warning ->
                        System.err.println("  - " + warning)
                );
            }

            // 3. Convert the issue to the desired format
            String convertedContent = issueConverter.convertToFormat(issue, outputFormat);

            // 4. Generate the filename
            String filename = generateFilename(issue.getKey(), outputFormat);

            // 5. Write the content
            var destination = ContentWriterPort.OutputDestination.file(outputDirectory, filename);
            contentWriter.writeContent(convertedContent, destination);

            // We know destination is FileDestination because we use OutputDestination.file()
            return destination.fullPath();

        } catch (org.matt.jiratomd.domain.ports.input.IssueFetcherPort.FetchException e) {
            throw new ConversionException(
                    "Failed to fetch issue: " + e.getMessage(),
                    fullIssueKey,
                    e
            );
        } catch (IssueConverterPort.ConversionException e) {
            throw new ConversionException(
                    "Failed to convert issue: " + e.getMessage(),
                    fullIssueKey,
                    e
            );
        } catch (ContentWriterPort.WriteException e) {
            throw new ConversionException(
                    "Failed to write output: " + e.getMessage(),
                    fullIssueKey,
                    e
            );
        } catch (IssueValidationService.ValidationException e) {
            throw new ConversionException(
                    "Invalid issue: " + e.getMessage(),
                    fullIssueKey,
                    e
            );
        }
    }

    /**
     * Converts an issue with default parameters
     */
    public Path convertIssue(String fullIssueKey, Path outputDirectory) throws ConversionException {
        return convertIssue(fullIssueKey, outputDirectory, IssueConverterPort.OutputFormat.MARKDOWN);
    }

    /**
     * Generates a filename from the issue key and format
     */
    private String generateFilename(String issueKey, IssueConverterPort.OutputFormat format) {
        String sanitizedKey = issueKey.replaceAll("[^a-zA-Z0-9.-]", "_");
        return sanitizedKey + "." + format.fileExtension();
    }

    /**
     * Specific exception for conversion errors
     */
    public static class ConversionException extends Exception {
        @SuppressWarnings("unused")
        private final String issueKey;

        public ConversionException(String message, String issueKey) {
            super(message);
            this.issueKey = issueKey;
        }

        public ConversionException(String message, String issueKey, Throwable cause) {
            super(message, cause);
            this.issueKey = issueKey;
        }
    }
}