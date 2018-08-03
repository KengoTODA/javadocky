package jp.skypencil.javadocky.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import jp.skypencil.javadocky.repository.LocalStorage;
import jp.skypencil.javadocky.repository.Storage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import reactor.test.StepVerifier;

public class JavadocExtractorTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void test() throws IOException {
    JavadocDownloader downloader =
        new JavadocDownloader(folder.newFolder("javadocky-javadoc").toPath());
    Path root = folder.newFolder("javadocky").toPath();
    Storage storage = new LocalStorage(root);
    JavadocExtractor extractor = new JavadocExtractor(downloader, storage);

    StepVerifier.create(
            storage.find("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html"))
        .expectComplete()
        .verify();
    File downloaded =
        extractor
            .extract("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html")
            .block();
    assertTrue(downloaded.isFile());
    StepVerifier.create(
            storage.find("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html"))
        .expectNextMatches(file -> file.equals(downloaded))
        .expectComplete()
        .verify();
  }
}
