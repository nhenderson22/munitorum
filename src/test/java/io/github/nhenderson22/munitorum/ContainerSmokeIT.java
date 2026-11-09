package io.github.nhenderson22.munitorum;

import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.client.ConnectionFactory;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@Testcontainers
class ContainerSmokeIT {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(TestImages.POSTGRES);

  @Container static final RabbitMQContainer RABBIT = new RabbitMQContainer(TestImages.RABBITMQ);

  @Test
  void postgresAnswersQueries() throws Exception {
    try (var conn =
            DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        var rs = conn.createStatement().executeQuery("SELECT 1")) {
      assertThat(rs.next()).isTrue();
      assertThat(rs.getInt(1)).isEqualTo(1);
    }
  }

  @Test
  void rabbitAcceptsAmqpConnections() throws Exception {
    var factory = new ConnectionFactory();
    factory.setUri(RABBIT.getAmqpUrl());
    factory.setUsername(RABBIT.getAdminUsername());
    factory.setPassword(RABBIT.getAdminPassword());

    try (var conn = factory.newConnection();
        var channel = conn.createChannel()) {
      var queue = channel.queueDeclare().getQueue();
      channel.basicPublish("", queue, null, "ping".getBytes());

      var response = channel.basicGet(queue, true);
      assertThat(response).isNotNull();
      assertThat(new String(response.getBody())).isEqualTo("ping");
    }
  }
}
