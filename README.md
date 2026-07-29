# Jira to Markdown

A Java Maven application that converts Jira tickets to Markdown files by calling the Jira REST API.

## Features

- Fetch Jira issues by ticket key
- Convert Jira data to structured Markdown format
- Save Markdown files locally
- Support for key fields: Key, Summary, Type, Description, Assignee, Creator, Dates, Status, Priority
- Error handling for network failures and invalid inputs
- Interactive CLI mode and command-line arguments
- Built with Java 25 using standard Maven
- Logging with SLF4J and Logback (replaces System.out/System.err)

## Prerequisites

- Java 25 or higher
- Maven 3.6 or higher
- Jira account with API access

## Jira API Token

To use this application, you need a Jira API token:

1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click "Create API token"
3. Copy the generated token
4. Use it with your Jira email as the username

## Project Structure

Standard Maven project structure:

```
jira-to-markdown/
├── pom.xml                    # Maven configuration for Java 25
├── README.md                  # Documentation
├── mvn-run.sh                # Runner script
├── run-example.sh            # Example runner
├── src/main/java/org/matt/jiratomd/
│   ├── JiraToMarkdownApp.java    # Main CLI application
│   ├── JiraApiClient.java        # Jira REST API client (uses Java 25 HttpClient)
│   ├── JiraConfig.java           # Configuration class
│   ├── JiraIssue.java            # Domain model with Jackson annotations
│   ├── User.java                 # User model
│   ├── IssueType.java            # Issue type model
│   ├── MarkdownConverter.java    # Markdown conversion logic
│   ├── FileWriter.java           # File output
│   ├── JiraApiException.java     # Custom exceptions
│   └── ExampleUsage.java         # Example implementation
├── src/main/resources/
│   └── logback.xml              # Logback configuration
└── src/test/java/org/matt/jiratomd/
    ├── MarkdownConverterTest.java
    ├── FileWriterTest.java
    └── JiraConfigTest.java
```

## Building with Maven

```bash
# Compile and run tests
mvn clean compile test

# Create executable JAR (fat JAR with dependencies)
mvn clean package assembly:single

# The JARs will be created at:
# target/jira-to-markdown-1.0-SNAPSHOT.jar                    # Standard JAR
# target/jira-to-markdown-1.0-SNAPSHOT-jar-with-dependencies.jar  # Fat JAR with all dependencies
```

## Usage

### Using the Fat JAR (Recommended)

```bash
# Build the fat JAR first
mvn clean package assembly:single

# Run the fat JAR (includes all dependencies)
java -jar target/jira-to-markdown-1.0-SNAPSHOT-jar-with-dependencies.jar \
    <baseUrl> <username> <apiToken> [issueKey] [outputDir]
```

### Using Maven to Run

```bash
# Run directly with Maven
mvn exec:java -Dexec.mainClass="org.matt.jiratomd.JiraToMarkdownApp" \
    -Dexec.args="<baseUrl> <username> <apiToken> [issueKey] [outputDir]"
```

### Using the Runner Script

```bash
# Make the script executable
chmod +x mvn-run.sh

# Run with the script
./mvn-run.sh <baseUrl> <username> <apiToken> [issueKey] [outputDir]
```

**Arguments:**
- `baseUrl`: Jira instance URL (e.g., `https://your-company.atlassian.net`)
- `username`: Jira username/email
- `apiToken`: Jira API token
- `issueKey`: Optional: Jira issue key (e.g., `PROJ-123`)
- `outputDir`: Optional: Output directory (default: `./output`)

### Examples

Process a single issue:
```bash
java -jar target/jira-to-markdown-1.0-SNAPSHOT-jar-with-dependencies.jar \
    https://company.atlassian.net user@email.com token123 PROJ-123 ./docs
```

Or using the runner script:
```bash
./mvn-run.sh https://company.atlassian.net user@email.com token123 PROJ-123 ./docs
```

Interactive mode (enter issue keys one by one):
```bash
java -jar target/jira-to-markdown-1.0-SNAPSHOT-jar-with-dependencies.jar \
    https://company.atlassian.net user@email.com token123
```

Show help/usage:
```bash
java -jar target/jira-to-markdown-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Run the example:
```bash
./run-example.sh
```

## Output Format

The generated Markdown file includes:

```markdown
# PROJ-123: Issue Summary

## Basic Information
- **Key**: PROJ-123
- **Summary**: Issue Summary
- **Type**: Bug
- **Status**: In Progress
- **Priority**: High

## Dates
- **Created**: 2024-01-15 10:30:00
- **Updated**: 2024-01-16 14:45:00

## People
- **Creator**: Jane Smith (jane@example.com)
- **Assignee**: John Doe (john@example.com)
- **Reporter**: Jane Smith (jane@example.com)

## Description
    Detailed issue description goes here...
```

## Logging

The application uses SLF4J with Logback for logging:
- All `System.out` and `System.err` calls have been replaced with proper logging
- Console output with timestamp and log level
- File logging to `jira-to-markdown.log`
- Configurable log levels in `src/main/resources/logback.xml`

Log levels:
- `ERROR`: Critical errors (authentication failures, network issues)
- `WARN`: Non-critical issues (date parsing failures)
- `INFO`: Standard operational messages (processing start/end, file writes)
- `DEBUG`: Detailed debugging information (HTTP requests, conversion details)

## Testing

Run the unit tests with:

```bash
mvn test
```

The project uses:
- JUnit 5 for testing
- AssertJ for fluent assertions
- No Spring dependencies (plain Java only)
- Tests include logging verification

## Dependencies (Managed by Maven)

- Jackson 2.18.3 (JSON processing only)
- SLF4J 2.0.17 (logging API)
- Logback 1.5.16 (logging implementation)
- JUnit 5.11.3 (testing)
- AssertJ 3.26.3 (assertions)
- Java 25 built-in HTTP client (no external HTTP library needed)

## Error Handling

The application handles:
- Invalid Jira credentials
- Network connectivity issues
- Invalid issue keys
- Missing required fields
- File system errors
- Timeouts and interruptions
- All errors are logged appropriately

## Example Implementation

See `src/main/java/org/matt/jiratomd/ExampleUsage.java` for a complete example showing how to use the components programmatically.

Run the example with:
```bash
./run-example.sh
```

## Changes from Original

- Added SLF4J with Logback as logging implementation
- Replaced all `System.out` and `System.err` calls with proper logging
- Added configuration file `logback.xml` for log management
- Added fat JAR generation with all dependencies included
- Added runner scripts for easier execution
- Updated tests to verify logging behavior
- Enhanced error messages with proper logging levels

## License

MIT