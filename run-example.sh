#!/bin/bash
# Script to run the example usage

echo "Compiling and running example..."
mvn clean compile exec:java -Dexec.mainClass="org.matt.jiratomd.ExampleUsage" -q