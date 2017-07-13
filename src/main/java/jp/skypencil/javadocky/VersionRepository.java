package jp.skypencil.javadocky;

import org.apache.maven.artifact.versioning.ArtifactVersion;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface VersionRepository {
    Mono<? extends ArtifactVersion> findLatest(String groupId, String artifactId);
    Flux<? extends ArtifactVersion> list(String groupId, String artifactId);
}
