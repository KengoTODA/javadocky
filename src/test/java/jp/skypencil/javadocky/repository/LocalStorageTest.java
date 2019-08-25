package jp.skypencil.javadocky.repository;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;

class LocalStorageTest {
  @Test
  void test(@TempDir Path root) throws IOException {
    Storage storage = new LocalStorage(root);
    assertNull(storage.find("g", "a", "v", "index.html").block());
    DataBufferFactory factory = new DefaultDataBufferFactory();
    storage
        .write(
            "g",
            "a",
            "v",
            "index.html",
            Flux.just(factory.wrap("Hello world!".getBytes(StandardCharsets.UTF_8))))
        .block();
    File written = storage.find("g", "a", "v", "index.html").block();
    assertNotNull(written);
    assertIterableEquals(Files.readAllLines(written.toPath()), Arrays.asList("Hello world!"));
  }
}
