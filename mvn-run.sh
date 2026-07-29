#!/bin/bash
# Script to run Jira to Markdown converter with Maven

if [ $# -lt 3 ]; then
    echo "Usage: $0 <baseUrl> <username> <apiToken> [issueKey] [outputDir]"
    echo ""
    echo "Examples:"
    echo "  $0 https://company.atlassian.net user@email.com token123 PROJ-123 ./docs"
    echo "  $0 https://company.atlassian.net user@email.com token123"
    echo ""
    echo "Note: You need a Jira API token from https://id.atlassian.com/manage-profile/security/api-tokens"
    exit 1
fi

BASE_URL=$1
USERNAME=$2
API_TOKEN=$3
ISSUE_KEY=$4
OUTPUT_DIR=${5:-"./output"}

ARGS="$BASE_URL $USERNAME $API_TOKEN"
if [ -n "$ISSUE_KEY" ]; then
    ARGS="$ARGS $ISSUE_KEY $OUTPUT_DIR"
fi

echo "Running Jira to Markdown converter..."
echo "Arguments: $ARGS"
echo ""

# First ensure the project is built
mvn clean package assembly:single > /dev/null 2>&1

# Run the fat JAR
java -jar target/jira-to-markdown-1.0-SNAPSHOT-jar-with-dependencies.jar $ARGS