package jp.skypencil.javadocky;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Optional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Storage {
    // TODO care about directory traversal
    Mono<Optional<File>> find(String groupId, String artifactId, String version, String path);
    Mono<File> write(String groupId, String artifactId, String version, String path, Flux<ByteBuffer> data);
}
