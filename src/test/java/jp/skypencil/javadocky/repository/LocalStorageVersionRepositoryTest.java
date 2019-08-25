package jp.skypencil.javadocky.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.test.StepVerifier;

class LocalStorageVersionRepositoryTest {
  @Test
  void test(@TempDir Path root) throws IOException {
    LocalStorageVersionRepository repository = new LocalStorageVersionRepository(root);
    StepVerifier.create(repository.findLatest("g", "a")).expectComplete().verify();

    File groupDir = new File(root.toFile(), "g");
    File artifactDir = new File(groupDir, "a");
    assertTrue(artifactDir.mkdirs());
    assertTrue(new File(artifactDir, "1.0.1").mkdir());
    StepVerifier.create(repository.findLatest("g", "a"))
        .expectNext(new DefaultArtifactVersion("1.0.1"))
        .expectComplete()
        .verify();

    assertTrue(new File(artifactDir, "1.0.0").mkdir());
    StepVerifier.create(repository.findLatest("g", "a"))
        .expectNext(new DefaultArtifactVersion("1.0.1"))
        .expectComplete()
        .verify();

    assertTrue(new File(artifactDir, "1.0.2").mkdir());
    StepVerifier.create(repository.findLatest("g", "a"))
        .expectNext(new DefaultArtifactVersion("1.0.2"))
        .expectComplete()
        .verify();

    assertTrue(new File(artifactDir, "1.1.0").mkdir());
    StepVerifier.create(repository.findLatest("g", "a"))
        .expectNext(new DefaultArtifactVersion("1.1.0"))
        .expectComplete()
        .verify();

    assertTrue(new File(artifactDir, "1.1.0-SNAPSHOT").mkdir());
    StepVerifier.create(repository.findLatest("g", "a"))
        .expectNext(new DefaultArtifactVersion("1.1.0"))
        .expectComplete()
        .verify();

    assertTrue(new File(artifactDir, "1.1.0-sp1").mkdir());
    StepVerifier.create(repository.findLatest("g", "a"))
        .expectNext(new DefaultArtifactVersion("1.1.0-sp1"))
        .expectComplete()
        .verify();
  }
}
