package com.github.xgp.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * When you are opening a file with try-with-resources that you want to
 * be deleted on close.
 */
public class TemporaryFile implements Closeable {

    private final Path path;

    public TemporaryFile(Path path) {
	this.path = path;
    }

    @Override public void close() throws IOException {
	Files.deleteIfExists(path);
    }

    public Path path() {
	return this.path;
    }

    @Override public String toString() {
	return String.format("TemporaryFile: %s", path);
    }

    public static TemporaryFile file() throws IOException {
	return new TemporaryFile(Files.createTempFile(null, null));
    }

    public static TemporaryFile empty() throws IOException {
	Path file = Files.createTempFile(null, null);
	Files.delete(file);
	return new TemporaryFile(file);
    }

}
