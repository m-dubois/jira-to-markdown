package org.matt.jiratomd.domain.ports.input;

import org.matt.jiratomd.domain.model.Issue;
import org.matt.jiratomd.domain.model.IssueId;

import java.util.Optional;

/**
 * Port for retrieving issues from different sources (Jira, GitLab, etc.)
 * Abstracts the data source for the domain
 */
public interface IssueFetcherPort {

    /**
     * Retrieves an issue by its identifier
     *
     * @param issueId Identifier of the issue to retrieve
     * @return An optional issue if found, empty otherwise
     * @throws FetchException If an error occurs during retrieval
     */
    Optional<Issue> fetchIssue(IssueId issueId) throws FetchException;

    /**
     * Business exception for issue retrieval errors
     */
    class FetchException extends Exception {
        private final IssueId issueId;

        public FetchException(IssueId issueId, String message) {
            super(message);
            this.issueId = issueId;
        }

        public FetchException(IssueId issueId, String message, Throwable cause) {
            super(message, cause);
            this.issueId = issueId;
        }

        @SuppressWarnings("unused")
        public IssueId getIssueId() {
            return issueId;
        }

        public static FetchException notFound(IssueId issueId) {
            return new FetchException(issueId, "Issue not found: " + issueId);
        }

        public static FetchException networkError(IssueId issueId, Throwable cause) {
            return new FetchException(issueId,
                    "Network error while fetching issue: " + issueId, cause);
        }

        public static FetchException authenticationError(IssueId issueId) {
            return new FetchException(issueId,
                    "Authentication failed while fetching issue: " + issueId);
        }

        public static FetchException authorizationError(IssueId issueId) {
            return new FetchException(issueId,
                    "Access forbidden while fetching issue: " + issueId);
        }
    }
}