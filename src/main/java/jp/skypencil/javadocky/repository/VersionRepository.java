package jp.skypencil.javadocky.repository;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A repository which stores version information.
 *
 * @author kengo
 */
public interface VersionRepository {
  /**
   * @param groupId Key to specify the target artifact
   * @param artifactId Key to specify the target artifact
   * @return non-null {@link Mono} which returns latest {@link ArtifactVersion} of target artifact.
   *     It will be empty if target artifact does not exist.
   */
  @NonNull
  Mono<ArtifactVersion> findLatest(@NonNull String groupId, @NonNull String artifactId);

  /**
   * @param groupId Key to specify the target artifact
   * @param artifactId Key to specify the target artifact
   * @return non-null {@link Flux} which returns all existing {@link ArtifactVersion} of target
   *     artifact. It will be empty if target artifact does not exist.
   */
  @NonNull
  Flux<ArtifactVersion> list(@NonNull String groupId, @NonNull String artifactId);
}
