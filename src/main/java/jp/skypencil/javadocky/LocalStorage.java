package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalStorage implements Storage {
  private final Logger log = LoggerFactory.getLogger(getClass());
  @NonNull private final Path root;

  @Autowired
  LocalStorage(@NonNull Path root) {
    this.root = Objects.requireNonNull(root);
  }

  @Override
  public Mono<File> find(String groupId, String artifactId, String version, String path) {
    File file = root.resolve(groupId).resolve(artifactId).resolve(version).resolve(path).toFile();
    if (file.isFile()) {
      return Mono.just(file);
    } else {
      return Mono.empty();
    }
  }

  @Override
  public Mono<File> write(
      String groupId, String artifactId, String version, String path, Flux<ByteBuffer> data) {
    File dir = root.resolve(groupId).resolve(artifactId).resolve(version).toFile();
    File file = new File(dir, path);
    File parent = file.getParentFile();
    if (parent == null) {
      return Mono.error(new IOException("Given path has no parent directory: " + file));
    } else if (!parent.isDirectory() && !parent.mkdirs()) {
      return Mono.error(new IOException("Failed to make directory at " + parent.getAbsolutePath()));
    }

    return Mono.create(
        subscriber -> {
          try {
            SeekableByteChannel channel =
                Files.newByteChannel(
                    file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            data.doFinally(
                    signal -> {
                      try {
                        channel.close();
                      } catch (IOException e) {
                        subscriber.error(e);
                      }
                    })
                .map(
                    buffer -> {
                      try {
                        return channel.write(buffer);
                      } catch (IOException e) {
                        throw new UncheckedIOException(e);
                      }
                    })
                .reduce((a, b) -> a + b)
                .subscribe(
                    total -> {
                      log.info("Written {} bytes data to {}", total, file.getAbsolutePath());
                      subscriber.success(file);
                    },
                    subscriber::error);
          } catch (IOException | RuntimeException e) {
            subscriber.error(e);
          }
        });
  }
}
