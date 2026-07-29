package org.matt.jiratomd.domain.model;

import java.util.Objects;

/**
 * Represents a user in the domain
 * Value Object - Immutable
 */
public record User(
        String accountId,
        String displayName,
        String emailAddress
) {

    public User {
        Objects.requireNonNull(displayName, "Display name cannot be null");
        // accountId can be null (anonymous users)
        // emailAddress can be null (not provided)
    }

    public boolean hasEmail() {
        return emailAddress != null && !emailAddress.isBlank();
    }

    public boolean isSameUser(User other) {
        if (other == null) return false;

        // Comparison by accountId if available
        if (this.accountId != null && other.accountId != null) {
            return this.accountId.equals(other.accountId);
        }

        // Fallback: comparison by email if available
        if (this.hasEmail() && other.hasEmail()) {
            return this.emailAddress.equalsIgnoreCase(other.emailAddress);
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(displayName);

        if (hasEmail()) {
            sb.append(" (").append(emailAddress).append(")");
        }

        return sb.toString();
    }

    public static User anonymous() {
        return new User(null, "Unassigned", null);
    }
}