package io.github.nhenderson22.munitorum;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class SchemaIT {

  @Container static final PostgreSQLContainer POSTGRES = TestDatabase.withSchema();

  @Test
  void schemaApplies() throws Exception {
    // Reaching this point means every file ran: a failing one stops the container from starting.
    try (var conn =
            DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        var rs = conn.createStatement().executeQuery("SELECT 1")) {
      assertThat(rs.next()).isTrue();
    }
  }
}
