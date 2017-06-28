package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class LocalStorage implements Storage {
    @NonNull
    @Getter
    private final Path root;

    @Override
    public Mono<Optional<File>> find(String groupId, String artifactId,
            String version, String path) {
        File file = root.resolve(groupId).resolve(artifactId).resolve(version)
                .resolve(path).toFile();
        if (file.isFile()) {
            return Mono.just(Optional.of(file));
        } else {
            return Mono.just(Optional.empty());
        }
    }

    @Override
    public Mono<File> write(String groupId, String artifactId, String version,
            String path, Flux<ByteBuffer> data) {
        File dir = root.resolve(groupId).resolve(artifactId).resolve(version)
                .toFile();
        File file = new File(dir, path);
        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }

        return Mono.create(subscriber -> {
            try {
                SeekableByteChannel channel = Files.newByteChannel(
                        file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                data.doFinally(signal -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        subscriber.error(e);
                    }
                })
                .map(buffer -> {
                    try {
                        return channel.write(buffer);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .reduce((a, b) -> a + b)
                .subscribe(
                        total -> {
                            log.info("Written {} bytes data to {}",
                                    total, file.getAbsolutePath());
                            subscriber.success(file);
                        }, subscriber::error);
            } catch (IOException | RuntimeException e) {
                subscriber.error(e);
            }
        });
    }
}
