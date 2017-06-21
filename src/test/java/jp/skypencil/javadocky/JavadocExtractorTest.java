package jp.skypencil.javadocky;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public class JavadocExtractorTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testUnzip() throws IOException {
        JavadocDownloader downloader = new JavadocDownloader();
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
        ByteBuffer firstData = result.blockLast().getT2().blockLast();
        assertThat(firstData, is(equalTo(ByteBuffer.wrap("World".getBytes(StandardCharsets.UTF_8)))));
    }

    @Test
    public void test() throws IOException {
        JavadocDownloader downloader = new JavadocDownloader();
        Path root = folder.newFolder("javadocky").toPath();
        Storage storage = new LocalStorage(root);
        JavadocExtractor extractor = new JavadocExtractor(downloader, storage);

        assertFalse(storage.find("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html").block().isPresent());
        extractor.extract("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3").block();
        assertTrue(storage.find("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html").block().isPresent());
    }

}
