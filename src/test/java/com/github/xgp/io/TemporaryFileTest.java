package com.github.xgp.io;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class TemporaryFileTest {

  @Test
  public void fileDeleteOnClose() throws Exception {
    Path path = null;
    try (TemporaryFile f = TemporaryFile.file()) {
      path = f.path();
      assertTrue(Files.exists(path));
      assertEquals(Files.size(path), 0);
      assertTrue(Files.isRegularFile(path));
    }
    assertFalse(Files.exists(path));

    try (TemporaryFile f = TemporaryFile.empty()) {
      path = f.path();
      assertFalse(Files.exists(path));
      Files.write(path, "test".getBytes());
      assertTrue(Files.exists(path));
      assertTrue(Files.isRegularFile(path));
    }
    assertFalse(Files.exists(path));
  }
}
