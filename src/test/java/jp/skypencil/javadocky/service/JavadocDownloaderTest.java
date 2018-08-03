package jp.skypencil.javadocky.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class JavadocDownloaderTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testDownload() throws IOException {
    Path root = folder.newFolder("javadocky-javadoc").toPath();
    Mono<Optional<File>> downloaded =
        new JavadocDownloader(root).download("com.github.spotbugs", "spotbugs", "3.1.0-RC3");
    StepVerifier.create(downloaded)
        .expectNextMatches(optional -> optional.get().length() == 7268250L)
        .expectComplete()
        .verify();
  }

  @Test
  public void testDownloadingMissingJavadoc() throws IOException {
    Path root = folder.newFolder("javadocky-javadoc").toPath();
    Mono<Optional<File>> downloaded =
        new JavadocDownloader(root).download("com.github.spotbugs", "spotbugs", "3.1.0-RC0");
    StepVerifier.create(downloaded)
        .expectNextMatches(optional -> !optional.isPresent())
        .expectComplete()
        .verify();
  }
}
