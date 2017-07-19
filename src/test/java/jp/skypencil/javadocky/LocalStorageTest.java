package jp.skypencil.javadocky;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import reactor.core.publisher.Flux;

public class LocalStorageTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        Path root = folder.newFolder("javadocky").toPath();
        Storage storage = new LocalStorage(root);
        assertThat(storage.find("g", "a", "v", "index.html").block(), is(nullValue()));
        storage.write("g", "a", "v", "index.html", Flux.just(ByteBuffer.wrap("Hello world!".getBytes(StandardCharsets.UTF_8)))).block();
        File written = storage.find("g", "a", "v", "index.html").block();
        assertThat(written, is(notNullValue()));
        assertThat(Files.readAllLines(written.toPath()), is(Arrays.asList("Hello world!")));
    }
}
