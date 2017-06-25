package jp.skypencil.javadocky;

import java.util.Optional;

import org.apache.maven.artifact.versioning.ArtifactVersion;

import reactor.core.publisher.Mono;

interface VersionRepository {
    Mono<Optional<? extends ArtifactVersion>> findLatest(String groupId, String artifactId);
}
