package org.matt.jiratomd;

import org.junit.jupiter.api.Test;
import org.matt.jiratomd.application.config.ApplicationContext;
import org.matt.jiratomd.infrastructure.config.JiraConfig;
import org.matt.jiratomd.presentation.cli.CliController;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the new hexagonal architecture components
 */
class HexagonalArchitectureTest {
    
    @Test
    void testApplicationContextCreation() {
        JiraConfig config = ApplicationContext.createTestConfig();
        assertNotNull(config);
        assertTrue(config.isValid());
    }
    
    @Test
    void testConfigValidation() {
        JiraConfig validConfig = new JiraConfig(
            "https://test.atlassian.net",
            "test@example.com",
            "test-token"
        );
        assertTrue(ApplicationContext.validateConfiguration(validConfig));
        
        // Test with invalid URL format - should throw exception
        assertThrows(IllegalArgumentException.class, () -> new JiraConfig("", "", ""));
    }
    
    @Test
    void testCliControllerCreation() {
        JiraConfig config = ApplicationContext.createTestConfig();
        CliController controller = ApplicationContext.createCliController(config);
        assertNotNull(controller);
    }
    
    @Test
    void testNewApplicationStartup() {
        // Verify that the new application class exists and can be loaded
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.JiraToMarkdownApplication"));
        
        // The new architecture uses CliController directly instead of testStartup
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.presentation.cli.CliController"));
    }
    
    @Test
    void testLegacyCompatibility() {
        // Verify that architecture layers exist
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.domain.model.Issue"));
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.domain.ports.input.IssueFetcherPort"));
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.application.usecases.ConvertJiraIssueToMarkdownUseCase"));
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.infrastructure.adapters.JiraApiAdapter"));
        assertDoesNotThrow(() -> Class.forName("org.matt.jiratomd.presentation.cli.CliController"));
    }
}