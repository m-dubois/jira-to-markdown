package org.matt.jiratomd.infrastructure.adapters.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraApiResponse {
    private String key;
    private Fields fields;

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @SuppressWarnings("unused")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("fields")
    public Fields getFields() {
        return fields;
    }

    @SuppressWarnings("unused")
    public void setFields(Fields fields) {
        this.fields = fields;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String summary;
        private String description;
        private IssueType issueType;
        private User assignee;
        private User creator;
        private User reporter;
        private String created;
        private String updated;
        private String status;
        private String priority;

        @JsonProperty("summary")
        public String getSummary() {
            return summary;
        }

        @SuppressWarnings("unused")
        public void setSummary(String summary) {
            this.summary = summary;
        }

        @JsonProperty("description")
        public String getDescription() {
            return description;
        }

        @SuppressWarnings("unused")
        public void setDescription(String description) {
            this.description = description;
        }

        @JsonProperty("issuetype")
        public IssueType getIssueType() {
            return issueType;
        }

        @SuppressWarnings("unused")
        public void setIssueType(IssueType issueType) {
            this.issueType = issueType;
        }

        @JsonProperty("assignee")
        public User getAssignee() {
            return assignee;
        }

        @SuppressWarnings("unused")
        public void setAssignee(User assignee) {
            this.assignee = assignee;
        }

        @JsonProperty("creator")
        public User getCreator() {
            return creator;
        }

        @SuppressWarnings("unused")
        public void setCreator(User creator) {
            this.creator = creator;
        }

        @JsonProperty("reporter")
        public User getReporter() {
            return reporter;
        }

        @SuppressWarnings("unused")
        public void setReporter(User reporter) {
            this.reporter = reporter;
        }

        @JsonProperty("created")
        public String getCreated() {
            return created;
        }

        @SuppressWarnings("unused")
        public void setCreated(String created) {
            this.created = created;
        }

        @JsonProperty("updated")
        public String getUpdated() {
            return updated;
        }

        @SuppressWarnings("unused")
        public void setUpdated(String updated) {
            this.updated = updated;
        }

        @JsonProperty("status")
        public String getStatus() {
            return status;
        }

        @SuppressWarnings("unused")
        public void setStatus(String status) {
            this.status = status;
        }

        @JsonProperty("priority")
        public String getPriority() {
            return priority;
        }

        @SuppressWarnings("unused")
        public void setPriority(String priority) {
            this.priority = priority;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {
        private String name;
        private String description;

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty("description")
        public String getDescription() {
            return description;
        }

        @SuppressWarnings("unused")
        public void setDescription(String description) {
            this.description = description;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String displayName;
        private String emailAddress;

        @JsonProperty("displayName")
        public String getDisplayName() {
            return displayName;
        }

        @SuppressWarnings("unused")
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @JsonProperty("emailAddress")
        public String getEmailAddress() {
            return emailAddress;
        }

        @SuppressWarnings("unused")
        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }
}