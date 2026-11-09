package io.github.nhenderson22.munitorum;

import java.time.Duration;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

public final class TestDatabase {

  private TestDatabase() {}

  // Get around the lsp error, plugin fixes this
  @SuppressWarnings("resource")
  public static PostgreSQLContainer withSchema() {
    return new PostgreSQLContainer(TestImages.POSTGRES)
        .withCopyFileToContainer(
            MountableFile.forHostPath("src/main/sql"), "/docker-entrypoint-initdb.d/")
        .withStartupTimeout(Duration.ofSeconds(30));
  }
}
