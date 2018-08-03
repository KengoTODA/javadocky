package jp.skypencil.javadocky.service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.ParametersAreNonnullByDefault;
import jp.skypencil.javadocky.repository.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is responsible to download javadoc.jar and unzip its contents onto {@link Storage}.
 */
@Service
public class JavadocExtractor {
  @NonNull private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

  @NonNull private final JavadocDownloader downloader;

  @NonNull private final Storage storage;

  @Autowired
  @ParametersAreNonnullByDefault
  JavadocExtractor(JavadocDownloader downloader, Storage storage) {
    this.downloader = Objects.requireNonNull(downloader);
    this.storage = Objects.requireNonNull(storage);
  }

  public Mono<File> extract(String groupId, String artifactId, String version, String path) {
    return downloader
        .download(groupId, artifactId, version)
        .flatMap(
            downloaded -> {
              if (!downloaded.isPresent()) {
                String message =
                    String.format(
                        "Javadoc.jar not found for %s:%s:%s", groupId, artifactId, version);
                return Mono.error(new IllegalArgumentException(message));
              }
              Flux<DataBuffer> flux = unzip(downloaded.get(), groupId, artifactId, version, path);
              return storage.write(groupId, artifactId, version, path, flux);
            });
  }

  private Flux<DataBuffer> unzip(
      File jar, String groupId, String artifactId, String version, String path) {
    try {
      ZipFile zip = new ZipFile(jar);
      ZipEntry entry = zip.getEntry(path);
      if (entry == null) {
        zip.close();
        String message =
            String.format(
                "%s not found in javadoc.jar of %s:%s:%s", path, groupId, artifactId, version);
        return Flux.error(new IllegalArgumentException(message));
      }
      return DataBufferUtils.readInputStream(
              () -> zip.getInputStream(entry), dataBufferFactory, 8 * 1024)
          .doFinally(
              signal -> {
                try {
                  zip.close();
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    } catch (IOException e) {
      return Flux.error(e);
    }
  }
}
