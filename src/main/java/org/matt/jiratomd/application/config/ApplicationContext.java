package org.matt.jiratomd.application.config;

import org.matt.jiratomd.application.usecases.ConvertJiraIssueToMarkdownUseCase;
import org.matt.jiratomd.domain.services.IssueValidationService;
import org.matt.jiratomd.infrastructure.adapters.FileSystemAdapter;
import org.matt.jiratomd.infrastructure.adapters.JiraApiAdapter;
import org.matt.jiratomd.infrastructure.adapters.MarkdownConverterAdapter;
import org.matt.jiratomd.infrastructure.config.JiraConfig;
import org.matt.jiratomd.presentation.cli.CliController;

/**
 * Application configuration and dependency injection container
 * Creates and wires all components together
 */
public class ApplicationContext {

    /**
     * Creates a fully configured CLI controller
     */
    public static CliController createCliController(JiraConfig config) {
        // Create infrastructure adapters
        JiraApiAdapter jiraAdapter = new JiraApiAdapter(config);
        MarkdownConverterAdapter markdownConverter = MarkdownConverterAdapter.create();
        FileSystemAdapter fileSystemAdapter = FileSystemAdapter.create();

        // Create domain service
        IssueValidationService validationService = new IssueValidationService();

        // Create use case
        ConvertJiraIssueToMarkdownUseCase convertUseCase = new ConvertJiraIssueToMarkdownUseCase(
                jiraAdapter,
                markdownConverter,
                fileSystemAdapter,
                validationService
        );

        // Create and return CLI controller
        return new CliController(convertUseCase, jiraAdapter, config);
    }

    /**
     * Creates configuration from command line arguments
     */
    public static JiraConfig createConfig(String baseUrl, String username, String apiToken) {
        return new JiraConfig(baseUrl, username, apiToken);
    }

    /**
     * Validates if the configuration is valid and the application can start
     */
    public static boolean validateConfiguration(JiraConfig config) {
        if (config == null) {
            return false;
        }

        try {
            // Basic validation
            return config.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a test configuration for development/demo purposes
     */
    public static JiraConfig createTestConfig() {
        return new JiraConfig(
                "https://test.atlassian.net",
                "test@example.com",
                "test-token-123"
        );
    }

    /**
     * Creates a configuration from environment variables
     */
    @SuppressWarnings("unused")
    public static JiraConfig createConfigFromEnvironment() {
        String baseUrl = System.getenv("JIRA_BASE_URL");
        String username = System.getenv("JIRA_USERNAME");
        String apiToken = System.getenv("JIRA_API_TOKEN");

        if (baseUrl == null || username == null || apiToken == null) {
            return null;
        }

        return new JiraConfig(baseUrl, username, apiToken);
    }
}