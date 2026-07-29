package org.matt.jiratomd.infrastructure.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.matt.jiratomd.domain.model.*;
import org.matt.jiratomd.domain.ports.input.IssueFetcherPort;
import org.matt.jiratomd.infrastructure.adapters.dto.JiraApiResponse;
import org.matt.jiratomd.infrastructure.config.JiraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapter for retrieving issues from the Jira API
 * Implements IssueFetcherPort
 */
public class JiraApiAdapter implements IssueFetcherPort {

    private static final Logger logger = LoggerFactory.getLogger(JiraApiAdapter.class);
    private static final String API_PATH = "/rest/api/latest/issue/";

    private final JiraConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public JiraApiAdapter(JiraConfig config) {
        this.config = Objects.requireNonNull(config, "JiraConfig cannot be null");
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        logger.debug("JiraApiAdapter initialized for base URL: {}", config.baseUrl());
    }

    @Override
    public Optional<Issue> fetchIssue(IssueId issueId) throws FetchException {
        Objects.requireNonNull(issueId, "Issue ID cannot be null");

        // This method requires the projectKey to build the complete key
        throw new FetchException(issueId,
                "Jira API requires full issue key (PROJECT-ID). " +
                        "Use fetchIssueByKey(String fullIssueKey) method instead.");
    }

    /**
     * Retrieves an issue with the complete key (PROJ-123)
     * Additional method not defined in the interface
     */
    public Optional<Issue> fetchIssueByKey(String fullIssueKey) throws FetchException {
        Objects.requireNonNull(fullIssueKey, "Full issue key cannot be null");

        IssueId issueId = extractIssueId(fullIssueKey);

        try {
            // Use the new direct implementation
            JiraApiResponse apiResponse = fetchFromJira(fullIssueKey);

            // Convert to domain entity
            Issue domainIssue = convertToDomain(apiResponse);

            logger.info("Successfully fetched issue: {}", fullIssueKey);
            return Optional.of(domainIssue);

        } catch (FetchException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching issue {}: {}", fullIssueKey, e.getMessage(), e);
            throw new FetchException(issueId, "Unexpected error: " + e.getMessage(), e);
        }
    }

    private JiraApiResponse fetchFromJira(String fullIssueKey) throws FetchException {
        if (!config.isValid()) {
            logger.error("Jira configuration is not valid");
            throw new FetchException(extractIssueId(fullIssueKey),
                    "Jira configuration is not valid. Please check baseUrl, username, and apiToken.");
        }

        String url = config.baseUrl() + API_PATH + fullIssueKey;
        logger.debug("Fetching issue from URL: {}", url);

        try {
            String auth = config.username() + ":" + config.apiToken();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            logger.debug("Sending HTTP request for issue: {}", fullIssueKey);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            logger.debug("Received HTTP response status: {}", statusCode);

            if (statusCode == 200) {
                logger.info("Successfully retrieved Jira issue: {}", fullIssueKey);
                return objectMapper.readValue(responseBody, JiraApiResponse.class);
            } else if (statusCode == 404) {
                logger.warn("Issue not found: {}", fullIssueKey);
                throw FetchException.notFound(extractIssueId(fullIssueKey));
            } else if (statusCode == 401) {
                logger.error("Authentication failed for user: {}", config.username());
                throw FetchException.authenticationError(extractIssueId(fullIssueKey));
            } else if (statusCode == 403) {
                logger.error("Access forbidden for user: {}", config.username());
                throw FetchException.authorizationError(extractIssueId(fullIssueKey));
            } else {
                logger.error("Jira API error: {} - Response: {}", statusCode, responseBody);
                throw new FetchException(extractIssueId(fullIssueKey),
                        "Jira API error: " + statusCode + " - " + responseBody);
            }
        } catch (FetchException fetchException) {
            logger.error("Failed to fetch issue from Jira: {}", fetchException.getMessage(), fetchException);
            throw fetchException;
        } catch (IOException ioException) {
            logger.error("Failed to fetch issue from Jira: {}", ioException.getMessage(), ioException);
            throw FetchException.networkError(extractIssueId(fullIssueKey), ioException);
        } catch (InterruptedException interruptedException) {
            logger.error("Request was interrupted: {}", interruptedException.getMessage(), interruptedException);
            Thread.currentThread().interrupt();
            throw new FetchException(extractIssueId(fullIssueKey),
                    "Request was interrupted: " + interruptedException.getMessage(), interruptedException);
        }
    }

    private Issue convertToDomain(JiraApiResponse apiResponse) {
        return Issue.from(
                IssueId.fromFullKey(apiResponse.getKey()),
                apiResponse.getKey(),
                IssueFields.builder()
                        .summary(apiResponse.getFields().getSummary())
                        .description(apiResponse.getFields().getDescription())
                        .issueType(convertIssueType(apiResponse.getFields().getIssueType()))
                        .assignee(convertUser(apiResponse.getFields().getAssignee()))
                        .creator(convertUser(apiResponse.getFields().getCreator()))
                        .reporter(convertUser(apiResponse.getFields().getReporter()))
                        .created(apiResponse.getFields().getCreated())
                        .updated(apiResponse.getFields().getUpdated())
                        .status(IssueStatus.fromString(apiResponse.getFields().getStatus()))
                        .priority(Priority.fromString(apiResponse.getFields().getPriority()))
                        .build(),
                null, // createdAt (to extract from the fields if possible)
                null  // updatedAt (to extract from the fields if possible)
        );
    }

    private IssueType convertIssueType(JiraApiResponse.IssueType apiIssueType) {
        if (apiIssueType == null) {
            return IssueType.fromString("Task"); // Default value
        }
        return IssueType.fromString(apiIssueType.getName());
    }

    private User convertUser(JiraApiResponse.User apiUser) {
        if (apiUser == null) {
            return null;
        }
        return new User(null, apiUser.getDisplayName(), apiUser.getEmailAddress());
    }

    private IssueId extractIssueId(String fullIssueKey) {
        try {
            return IssueId.fromFullKey(fullIssueKey);
        } catch (IllegalArgumentException e) {
            return new IssueId(0); // Invalid ID
        }
    }
}