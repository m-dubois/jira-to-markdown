package org.matt.jiratomd.infrastructure.config;

import java.util.Objects;

/**
 * Configuration for the Jira API
 * Value Object - Immutable
 */
public record JiraConfig(
        String baseUrl,
        String username,
        String apiToken
) {

    public JiraConfig {
        Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(apiToken, "API token cannot be null");

        // Basic URL validation
        if (!baseUrl.toLowerCase().startsWith("http")) {
            throw new IllegalArgumentException("Base URL must start with http:// or https://");
        }

        // Remove trailing slash if present
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
    }

    public boolean isValid() {
        return !baseUrl.isBlank() &&
                !username.isBlank() &&
                !apiToken.isBlank();
    }

    /**
     * Creates Basic Auth authentication header
     */
    @SuppressWarnings("unused")
    public String getBasicAuthHeader() {
        String auth = username + ":" + apiToken;
        return java.util.Base64.getEncoder().encodeToString(auth.getBytes());
    }

    /**
     * Builds the complete URL for a Jira resource
     */
    @SuppressWarnings("unused")
    public String buildUrl(String path) {
        Objects.requireNonNull(path, "Path cannot be null");

        if (path.startsWith("/")) {
            return baseUrl + path;
        } else {
            return baseUrl + "/" + path;
        }
    }

    /**
     * Builds the URL to retrieve a specific issue
     */
    @SuppressWarnings("unused")
    public String buildIssueUrl(String issueKey) {
        Objects.requireNonNull(issueKey, "Issue key cannot be null");
        return buildUrl("/rest/api/latest/issue/" + issueKey);
    }
}