package org.matt.jiratomd.domain.ports.input;

import org.matt.jiratomd.domain.model.Issue;

/**
 * Port for converting issues into different formats (Markdown, HTML, PDF, etc.)
 * Abstracts the output format for the domain
 */
public interface IssueConverterPort {

    /**
     * Supported output formats
     */
    enum OutputFormat {
        MARKDOWN("md"),
        HTML("html"),
        PLAIN_TEXT("txt"),
        JSON("json");

        private final String fileExtension;

        OutputFormat(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String fileExtension() {
            return fileExtension;
        }

        @SuppressWarnings("unused")
        public static OutputFormat fromExtension(String extension) {
            if (extension == null) {
                return MARKDOWN;
            }

            String lowerExt = extension.toLowerCase().replace(".", "");
            for (OutputFormat format : values()) {
                if (format.fileExtension.equals(lowerExt)) {
                    return format;
                }
            }
            return MARKDOWN; // By default
        }
    }

    /**
     * Converts an issue into a specific format
     *
     * @param issue  Issue to convert
     * @param format Desired output format
     * @return The formatted content of the issue
     * @throws ConversionException If the conversion fails
     */
    String convertToFormat(Issue issue, OutputFormat format) throws ConversionException;

    /**
     * Business exception for conversion errors
     */
    class ConversionException extends Exception {
        private final String issueKey;
        private final OutputFormat format;

        public ConversionException(String issueKey, OutputFormat format, String message) {
            super(message);
            this.issueKey = issueKey;
            this.format = format;
        }

        public ConversionException(String issueKey, OutputFormat format, String message, Throwable cause) {
            super(message, cause);
            this.issueKey = issueKey;
            this.format = format;
        }

        @SuppressWarnings("unused")
        public String getIssueKey() {
            return issueKey;
        }

        @SuppressWarnings("unused")
        public OutputFormat getFormat() {
            return format;
        }

        public static ConversionException illegalFormat(String issueKey, OutputFormat format) {
            return new ConversionException(issueKey, format,
                    "Unsupported output format: " + format);
        }

        @SuppressWarnings("unused")
        public static ConversionException invalidIssue(String issueKey, OutputFormat format) {
            return new ConversionException(issueKey, format,
                    "Cannot convert invalid issue: " + issueKey);
        }
    }
}