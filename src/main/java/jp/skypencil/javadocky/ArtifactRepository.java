package jp.skypencil.javadocky;

import reactor.core.publisher.Flux;

interface ArtifactRepository {
    Flux<String> list(String groupId);
}
