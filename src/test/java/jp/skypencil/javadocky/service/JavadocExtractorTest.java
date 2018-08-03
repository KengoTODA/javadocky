package jp.skypencil.javadocky.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jp.skypencil.javadocky.repository.LocalStorage;
import jp.skypencil.javadocky.repository.Storage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

public class JavadocExtractorTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testUnzip() throws IOException {
    JavadocDownloader downloader =
        new JavadocDownloader(folder.newFolder("javadocky-javadoc").toPath());
    Path root = folder.newFolder("javadocky").toPath();
    Storage storage = new LocalStorage(root);
    JavadocExtractor extractor = new JavadocExtractor(downloader, storage);

    File zip = folder.newFile();
    try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(zip))) {
      output.putNextEntry(new ZipEntry("hello.txt"));
      output.write("Hello".getBytes(StandardCharsets.UTF_8));
      output.closeEntry();
      output.putNextEntry(new ZipEntry("world.txt"));
      output.write("World".getBytes(StandardCharsets.UTF_8));
      output.closeEntry();
    }
    Flux<Tuple2<String, Flux<ByteBuffer>>> result = extractor.unzip(zip);
    StepVerifier.create(result)
        .expectNextMatches(
            tuple ->
                tuple.getT1().equals("hello.txt")
                    && tuple
                        .getT2()
                        .blockFirst()
                        .equals(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8))))
        .expectNextMatches(
            tuple ->
                tuple.getT1().equals("world.txt")
                    && tuple
                        .getT2()
                        .blockFirst()
                        .equals(ByteBuffer.wrap("World".getBytes(StandardCharsets.UTF_8))))
        .expectComplete()
        .verify();
  }

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
