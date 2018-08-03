package jp.skypencil.javadocky;

import java.io.File;
import java.nio.ByteBuffer;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Storage {
  // TODO care about directory traversal
  /**
   * Find the file from javadoc for specified artifact.
   *
   * @param groupId non-null groupId to specify target artifact
   * @param artifactId non-null artifactId to specify target artifact
   * @param version non-null version to specify target artifact
   * @param path non-null path of target file, e.g. "index.html", "foo/bar.css"
   * @return A non-null {@link Mono} which contains found file. It can be empty if no specified file
   *     in this storage.
   */
  @NonNull
  Mono<File> find(
      @NonNull String groupId,
      @NonNull String artifactId,
      @NonNull String version,
      @NonNull String path);

  /**
   * Store a file into storage.
   *
   * @param groupId non-null groupId to specify target artifact
   * @param artifactId non-null artifactId to specify target artifact
   * @param version non-null version to specify target artifact
   * @param path non-null path of target file, e.g. "index.html", "foo/bar.css"
   * @param data stream of data to store onto this storage
   * @return A non-null {@link Mono} which contains stored file.
   */
  @NonNull
  Mono<File> write(
      @NonNull String groupId,
      @NonNull String artifactId,
      @NonNull String version,
      @NonNull String path,
      @NonNull Flux<ByteBuffer> data);
}
