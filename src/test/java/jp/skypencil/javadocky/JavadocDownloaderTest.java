package jp.skypencil.javadocky;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

import reactor.core.publisher.Mono;

public class JavadocDownloaderTest {

    @Test
    public void test() {
        Mono<File> downloaded = new JavadocDownloader().download("com.github.spotbugs", "spotbugs", "3.1.0-RC3");
        File file = downloaded.block();
        assertThat(file.length(), is(7268250L));
    }

}
