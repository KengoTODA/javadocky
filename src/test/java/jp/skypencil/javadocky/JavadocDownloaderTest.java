package jp.skypencil.javadocky;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import reactor.core.publisher.Mono;

public class JavadocDownloaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDownload() throws IOException {
        Path root = folder.newFolder("javadocky-javadoc").toPath();
        Mono<Optional<File>> downloaded = new JavadocDownloader(root).download("com.github.spotbugs", "spotbugs", "3.1.0-RC3");
        File file = downloaded.block().get();
        assertThat(file.length(), is(7268250L));
    }

    @Test
    public void testDownloadingMissingJavadoc() throws IOException {
        Path root = folder.newFolder("javadocky-javadoc").toPath();
        Mono<Optional<File>> downloaded = new JavadocDownloader(root).download("com.github.spotbugs", "spotbugs", "3.1.0-RC0");
        assertFalse(downloaded.block().isPresent());
    }

}
