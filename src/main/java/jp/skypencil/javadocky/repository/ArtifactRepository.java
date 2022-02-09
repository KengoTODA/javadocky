package jp.skypencil.javadocky.repository;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

/**
 * A repository which handles artifact information.
 *
 * @author kengo
 */
public interface ArtifactRepository {
  /**
   * Returns a non-null {@link Flux} of {@code artifactId} belonging to specified {@code groupId}.
   *
   * @param groupId Target {@code groupId} to list up {@code artifactId}.
   */
  @NonNull
  Flux<String> list(@NonNull String groupId);
}
