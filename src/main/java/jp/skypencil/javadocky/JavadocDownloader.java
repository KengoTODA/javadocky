package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class JavadocDownloader {
    private static final String REPO_URL = "http://central.maven.org/maven2/";
    private WebClient webClient = WebClient.create(REPO_URL);

    Mono<Optional<File>> download(String groupId, String artifactId, String version) {
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("groupId", groupId.replace(".", "/"));
        uriVariables.put("artifactId", artifactId);
        uriVariables.put("version", version);

        Mono<ClientResponse> response = webClient.method(HttpMethod.GET)
            .uri("{groupId}/{artifactId}/{version}/{artifactId}-{version}-javadoc.jar", uriVariables)
            .accept(MediaType.parseMediaType("application/java-archive"))
            .exchange();
        return response.flatMap(this::fetchResponse);
    }

    private Mono<Optional<File>> fetchResponse(ClientResponse res) {
        HttpStatus status = res.statusCode();
        if (status == HttpStatus.NOT_FOUND) {
            return Mono.just(Optional.empty());
        } else if (!status.is2xxSuccessful()) {
            return Mono.error(new IllegalArgumentException("Unexpected status code:" + status.value()));
        }
        return storeToLocal(res.body(BodyExtractors.toDataBuffers())).map(Optional::of);
    }

    private Mono<File> storeToLocal(Flux<DataBuffer> flux) {
        try {
            File file = File.createTempFile("downloaded", ".jar");
            return flux.collect(this.open(file), (FileChannel channel, DataBuffer data) -> {
                try {
                    channel.write(data.asByteBuffer());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).map(channel -> file);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    private Supplier<FileChannel> open(File file) {
        return () -> {
            try {
                return FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
