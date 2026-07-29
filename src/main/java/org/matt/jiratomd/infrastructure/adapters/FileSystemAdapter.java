package org.matt.jiratomd.infrastructure.adapters;

import org.matt.jiratomd.domain.ports.output.ContentWriterPort;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Adapter for writing content to the file system
 * Implements ContentWriterPort
 */
public class FileSystemAdapter implements ContentWriterPort {

    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(FileSystemAdapter.class);

    @Override
    public void writeContent(String content, OutputDestination destination) throws WriteException {
        Objects.requireNonNull(content, "Content cannot be null");
        Objects.requireNonNull(destination, "Destination cannot be null");

        if (destination instanceof ConsoleDestination) {
            writeToConsole(content);
        } else if (destination instanceof FileDestination fileDest) {
            writeToFile(content, fileDest);
        } else {
            throw new WriteException(destination, "Unsupported destination type: " + destination.getClass().getSimpleName());
        }
    }

    private void writeToConsole(String content) {
        System.out.println(content);
        logger.info("Content written to console ({} characters)", content.length());
    }

    private void writeToFile(String content, FileDestination destination) throws WriteException {
        Path filePath = destination.fullPath();
        Path directory = filePath.getParent();

        try {
            // Create directory if necessary
            if (directory != null && !Files.exists(directory)) {
                Files.createDirectories(directory);
                logger.info("Created output directory: {}", directory);
            }

            // Write the content
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                writer.write(content);
            }

            logger.info("Successfully wrote file: {} ({} characters)",
                    filePath, content.length());

        } catch (IOException e) {
            // Handle specific errors
            if (e.getMessage() != null && e.getMessage().contains("Permission denied")) {
                throw WriteException.permissionDenied(destination);
            } else if (e.getMessage() != null && e.getMessage().contains("No space left on device")) {
                throw WriteException.diskFull(destination);
            } else {
                throw WriteException.fileSystemError(destination, e);
            }
        }
    }

    /**
     * Factory to create the adapter
     */
    public static FileSystemAdapter create() {
        return new FileSystemAdapter();
    }
}