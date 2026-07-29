package org.matt.jiratomd;

import org.matt.jiratomd.presentation.cli.CliController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class using the new hexagonal architecture
 * This is the entry point that should be used going forward
 */
public class JiraToMarkdownApplication {
    private static final Logger logger = LoggerFactory.getLogger(JiraToMarkdownApplication.class);

    /**
     * Main entry point for the new hexagonal architecture
     */
    static void main(String[] args) {
        logger.info("Jira to Markdown Converter (Hexagonal Architecture)");
        logger.info("=====================================================");

        try {
            // Use the new CLI controller from the presentation layer
            CliController controller = new CliController(null, null, null);
            controller.run(args);
        } catch (Exception e) {
            logger.error("Fatal application error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

}