package org.matt.jiratomd.presentation.cli;

import org.matt.jiratomd.application.config.ApplicationContext;
import org.matt.jiratomd.application.usecases.ConvertJiraIssueToMarkdownUseCase;
import org.matt.jiratomd.infrastructure.adapters.JiraApiAdapter;
import org.matt.jiratomd.infrastructure.config.JiraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * CLI Controller for the Jira to Markdown application
 * Presentation layer component that handles user interaction
 */
public class CliController {
    private static final Logger logger = LoggerFactory.getLogger(CliController.class);
    private static final String DEFAULT_OUTPUT_DIR = "./output";

    private final ConvertJiraIssueToMarkdownUseCase convertUseCase;
    private final JiraConfig jiraConfig;

    /**
     * Creates a CLI controller with dependency injection
     */
    @SuppressWarnings("unused")
    public CliController(
            ConvertJiraIssueToMarkdownUseCase convertUseCase,
            JiraApiAdapter jiraApiAdapter,
            JiraConfig jiraConfig
    ) {
        this.convertUseCase = convertUseCase;
        this.jiraConfig = jiraConfig;
    }

    /**
     * Main entry point for the CLI application
     */
    public void run(String[] args) {
        try {
            if (args.length < 3) {
                printUsage();
                return;
            }

            // Parse command line arguments
            String baseUrl = args[0];
            String username = args[1];
            String apiToken = args[2];
            String issueKey = args.length > 3 ? args[3] : null;
            String outputDir = args.length > 4 ? args[4] : DEFAULT_OUTPUT_DIR;

            // Create configuration
            JiraConfig config = ApplicationContext.createConfig(baseUrl, username, apiToken);

            // Validate configuration
            if (!ApplicationContext.validateConfiguration(config)) {
                logger.error("Invalid Jira configuration. Please check your credentials.");
                return;
            }

            // Create controller through ApplicationContext
            CliController controller = ApplicationContext.createCliController(config);

            // Execute based on input
            if (issueKey != null) {
                controller.processSingleIssue(issueKey, outputDir);
            } else {
                controller.interactiveMode(outputDir);
            }

        } catch (Exception e) {
            logger.error("Application error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Process a single issue
     */
    public void processSingleIssue(String issueKey, String outputDir) {
        logger.info("Processing Jira issue: {}", issueKey);

        try {
            // Validate output directory
            Path outputPath = Paths.get(outputDir).toAbsolutePath();
            ensureOutputDirectoryExists(outputPath);

            // Execute use case
            Path resultPath = convertUseCase.convertIssue(issueKey, outputPath);

            logger.info("Successfully created: {}", resultPath);
            logger.info("Processing completed successfully!");

        } catch (ConvertJiraIssueToMarkdownUseCase.ConversionException e) {
            logger.error("Conversion error: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.debug("Root cause: {}", e.getCause().getMessage());
            }
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    /**
     * Interactive mode for processing multiple issues
     */
    public void interactiveMode(String outputDir) {
        logger.info("Jira to Markdown Converter");
        logger.info("===========================");
        logger.info("Configuration:");
        logger.info("- Base URL: {}", jiraConfig.baseUrl());
        logger.info("- Username: {}", jiraConfig.username());
        logger.info("- Output Directory: {}", outputDir);
        logger.info("");
        logger.info("Enter Jira issue keys (one per line). Type 'exit' to quit.");

        Path outputPath = Paths.get(outputDir).toAbsolutePath();

        try {
            ensureOutputDirectoryExists(outputPath);
        } catch (IOException e) {
            logger.error("Cannot create output directory: {}", e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        try (scanner) {
            while (true) {
                System.out.print("Enter issue key (e.g., PROJ-123): ");
                String issueKey = scanner.nextLine().trim();

                if (issueKey.equalsIgnoreCase("exit") || issueKey.equalsIgnoreCase("quit")) {
                    logger.info("Goodbye!");
                    break;
                }

                if (issueKey.isEmpty()) {
                    continue;
                }

                try {
                    processSingleIssue(issueKey, outputDir);
                    logger.info("");
                } catch (Exception e) {
                    logger.error("Failed to process issue {}: {}", issueKey, e.getMessage());
                }
            }
        }
    }

    /**
     * Process multiple issues in batch mode
     */
    @SuppressWarnings("unused")
    public void batchMode(String[] issueKeys, String outputDir) {
        logger.info("Processing {} issues in batch mode", issueKeys.length);
        logger.info("Output directory: {}", outputDir);

        Path outputPath = Paths.get(outputDir).toAbsolutePath();

        try {
            ensureOutputDirectoryExists(outputPath);
        } catch (IOException e) {
            logger.error("Cannot create output directory: {}", e.getMessage());
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (String issueKey : issueKeys) {
            logger.info("Processing: {}", issueKey);

            try {
                convertUseCase.convertIssue(issueKey, outputPath);
                successCount++;
                logger.info("✓ Success: {}", issueKey);
            } catch (ConvertJiraIssueToMarkdownUseCase.ConversionException e) {
                failureCount++;
                logger.error("✗ Failed: {} - {}", issueKey, e.getMessage());
            }
        }

        logger.info("");
        logger.info("Batch processing completed:");
        logger.info("- Successful: {}", successCount);
        logger.info("- Failed: {}", failureCount);
        logger.info("- Total: {}", issueKeys.length);
    }

    /**
     * Ensures the output directory exists, creates it if necessary
     */
    private void ensureOutputDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            logger.info("Created output directory: {}", directory);
        }

        if (!Files.isWritable(directory)) {
            throw new IOException("Output directory is not writable: " + directory);
        }
    }

    /**
     * Prints usage information
     */
    public static void printUsage() {
        logger.info("Jira to Markdown Converter (Hexagonal Architecture)");
        logger.info("=====================================================");
        logger.info("Usage:");
        logger.info("  java -jar jira-to-markdown.jar <baseUrl> <username> <apiToken> [issueKey] [outputDir]");
        logger.info("");
        logger.info("Arguments:");
        logger.info("  baseUrl    - Jira instance URL (e.g., https://your-company.atlassian.net)");
        logger.info("  username   - Jira username/email");
        logger.info("  apiToken   - Jira API token");
        logger.info("  issueKey   - Optional: Jira issue key (e.g., PROJ-123)");
        logger.info("  outputDir  - Optional: Output directory (default: ./output)");
        logger.info("");
        logger.info("Examples:");
        logger.info("  Basic usage (interactive mode):");
        logger.info("    java -jar jira-to-markdown.jar https://company.atlassian.net user@email.com token123");
        logger.info("");
        logger.info("  Single issue:");
        logger.info("    java -jar jira-to-markdown.jar https://company.atlassian.net user@email.com token123 PROJ-123 ./docs");
        logger.info("");
        logger.info("  Batch mode (through configuration file - future feature):");
        logger.info("    java -jar jira-to-markdown.jar --batch issues.txt ./output");
        logger.info("");
        logger.info("Environment variables (alternative to command line):");
        logger.info("  JIRA_BASE_URL, JIRA_USERNAME, JIRA_API_TOKEN");
    }

    /**
     * Main method - application entry point
     */
    static void main(String[] args) {
        // Check for environment variables if command line args are missing
        if (args.length < 3) {
            String baseUrl = System.getenv("JIRA_BASE_URL");
            String username = System.getenv("JIRA_USERNAME");
            String apiToken = System.getenv("JIRA_API_TOKEN");

            if (baseUrl != null && username != null && apiToken != null) {
                // Build new args array with environment variables
                String[] envArgs = new String[Math.max(args.length + 3, 3)];
                envArgs[0] = baseUrl;
                envArgs[1] = username;
                envArgs[2] = apiToken;

                // Copy any additional args
                System.arraycopy(args, 0, envArgs, 3, args.length);
                args = envArgs;
            }
        }

        // Create a minimal controller for startup
        CliController controller = new CliController(null, null, null);
        controller.run(args);
    }
}