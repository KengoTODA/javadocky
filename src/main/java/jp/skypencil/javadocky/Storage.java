package jp.skypencil.javadocky;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Optional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Storage {
    Mono<Optional<File>> find(String groupId, String artifactId, String version, String path);
    Mono<Void> write(String groupId, String artifactId, String version, String path, Flux<ByteBuffer> data);
}
