package jp.skypencil.javadocky.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JavadocDownloaderTest {
  private static final String MAVEN_REPO = "https://repo.maven.apache.org/maven2/";

  @Test
  void testDownload(@TempDir Path root) throws IOException {
    Mono<Optional<File>> downloaded =
        new JavadocDownloader(root, MAVEN_REPO)
            .download("com.github.spotbugs", "spotbugs", "3.1.0-RC3");
    StepVerifier.create(downloaded)
        .expectNextMatches(optional -> optional.get().length() == 7268250L)
        .expectComplete()
        .verify();
  }

  @Test
  void testDownloadingMissingJavadoc(@TempDir Path root) throws IOException {
    Mono<Optional<File>> downloaded =
        new JavadocDownloader(root, MAVEN_REPO)
            .download("com.github.spotbugs", "spotbugs", "3.1.0-RC0");
    StepVerifier.create(downloaded)
        .expectNextMatches(optional -> !optional.isPresent())
        .expectComplete()
        .verify();
  }
}
