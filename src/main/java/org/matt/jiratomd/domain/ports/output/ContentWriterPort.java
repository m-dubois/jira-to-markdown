package org.matt.jiratomd.domain.ports.output;

import java.nio.file.Path;

/**
 * Port for writing content to different destinations (files, S3, console, etc.)
 * Abstracts the output destination for the domain
 */
public interface ContentWriterPort {

    /**
     * Supported output destinations
     */
    interface OutputDestination {

        static FileDestination file(Path directory, String filename) {
            return new FileDestination(directory, filename);
        }

        @SuppressWarnings("unused")
        static FileDestination file(String directory, String filename) {
            return new FileDestination(Path.of(directory), filename);
        }

        @SuppressWarnings("unused")
        static ConsoleDestination console() {
            return new ConsoleDestination();
        }
    }

    /**
     * Destination: File system
     */
    record FileDestination(Path directory, String filename) implements OutputDestination {
        public FileDestination {
            if (directory == null) {
                throw new IllegalArgumentException("Directory cannot be null");
            }
            if (filename == null || filename.isBlank()) {
                throw new IllegalArgumentException("Filename cannot be null or blank");
            }
        }

        public Path fullPath() {
            return directory.resolve(filename);
        }
    }

    /**
     * Destination: Console
     */
    record ConsoleDestination() implements OutputDestination {
    }

    /**
     * Writes content to a specific destination
     *
     * @param content     Content to write
     * @param destination Output destination
     * @throws WriteException If writing fails
     */
    void writeContent(String content, OutputDestination destination) throws WriteException;

    /**
     * Business exception for writing errors
     */
    class WriteException extends Exception {
        private final OutputDestination destination;

        public WriteException(OutputDestination destination, String message) {
            super(message);
            this.destination = destination;
        }

        public WriteException(OutputDestination destination, String message, Throwable cause) {
            super(message, cause);
            this.destination = destination;
        }

        @SuppressWarnings("unused")
        public OutputDestination getDestination() {
            return destination;
        }

        public static WriteException fileSystemError(OutputDestination destination, Throwable cause) {
            String message = "File system error";
            if (destination instanceof FileDestination fileDest) {
                message = "Cannot write to file: " + fileDest.fullPath();
            }
            return new WriteException(destination, message, cause);
        }

        public static WriteException permissionDenied(OutputDestination destination) {
            String message = "Permission denied";
            if (destination instanceof FileDestination fileDest) {
                message = "Permission denied for file: " + fileDest.fullPath();
            }
            return new WriteException(destination, message);
        }

        public static WriteException diskFull(OutputDestination destination) {
            String message = "Disk full";
            if (destination instanceof FileDestination fileDest) {
                message = "Disk full, cannot write to: " + fileDest.fullPath();
            }
            return new WriteException(destination, message);
        }
    }
}