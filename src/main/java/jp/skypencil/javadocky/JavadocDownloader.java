package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
class JavadocDownloader {
    private static final String REPO_URL = "http://central.maven.org/maven2/";
    private final WebClient webClient = WebClient.create(REPO_URL);

    private final Path root;

    Mono<Optional<File>> download(String groupId, String artifactId, String version) {
        Path path = pathFor(groupId, artifactId, version);
        if (path.toFile().isFile()) {
            return Mono.just(Optional.of(path.toFile()));
        }

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("groupId", groupId.replace(".", "/"));
        uriVariables.put("artifactId", artifactId);
        uriVariables.put("version", version);

        Mono<ClientResponse> response = webClient.method(HttpMethod.GET)
            .uri("{groupId}/{artifactId}/{version}/{artifactId}-{version}-javadoc.jar", uriVariables)
            .accept(MediaType.parseMediaType("application/java-archive"))
            .exchange();
        return response.flatMap(fetchResponse(path));
    }

    private Function<ClientResponse, Mono<Optional<File>>> fetchResponse(Path path) {
        return res -> {
            HttpStatus status = res.statusCode();
            if (status == HttpStatus.NOT_FOUND) {
                return Mono.just(Optional.empty());
            } else if (!status.is2xxSuccessful()) {
                return Mono.error(new IllegalArgumentException("Unexpected status code:" + status.value()));
            }
            return storeToLocal(res.body(BodyExtractors.toDataBuffers()), path).map(Optional::of);
        };
    }

    private Mono<File> storeToLocal(Flux<DataBuffer> flux, Path path) {
        return flux.collect(this.open(path), (FileChannel channel, DataBuffer data) -> {
            try {
                channel.write(data.asByteBuffer());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).map(channel -> path.toFile());
    }

    private Supplier<FileChannel> open(Path path) {
        return () -> {
            File parent = path.toFile().getParentFile();

            try {
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to make directory at " + parent.getAbsolutePath());
                }
                return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private Path pathFor(String groupId, String artifactId, String version) {
        return root.resolve(Paths.get(groupId, artifactId, version + ".jar"));
    }
}
