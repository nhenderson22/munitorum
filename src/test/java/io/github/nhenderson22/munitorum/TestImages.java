package io.github.nhenderson22.munitorum;

import org.testcontainers.utility.DockerImageName;

/**
 * Container images used by the integration tests. Keep these in sync with compose.yaml.
 *
 * <p>Names are fully qualified so Podman never has to resolve a short name (it can't prompt
 * mid-test). asCompatibleSubstituteFor tells Testcontainers these are the images it expects.
 */
public final class TestImages {

  public static final DockerImageName POSTGRES =
      DockerImageName.parse("docker.io/library/postgres:18").asCompatibleSubstituteFor("postgres");

  public static final DockerImageName RABBITMQ =
      DockerImageName.parse("docker.io/library/rabbitmq:4-management")
          .asCompatibleSubstituteFor("rabbitmq");

  private TestImages() {}
}
