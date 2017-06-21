package jp.skypencil.javadocky;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Optional;

import org.junit.Test;

import reactor.core.publisher.Mono;

public class JavadocDownloaderTest {

    @Test
    public void testDownload() {
        Mono<Optional<File>> downloaded = new JavadocDownloader().download("com.github.spotbugs", "spotbugs", "3.1.0-RC3");
        File file = downloaded.block().get();
        assertThat(file.length(), is(7268250L));
    }

    @Test
    public void testDownloadingMissingJavadoc() {
        Mono<Optional<File>> downloaded = new JavadocDownloader().download("com.github.spotbugs", "spotbugs", "3.1.0-RC0");
        assertFalse(downloaded.block().isPresent());
    }

}
