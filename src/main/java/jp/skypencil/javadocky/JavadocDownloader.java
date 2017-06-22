package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
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
        return res.body(new ExtractToFile()).map(Optional::of);
    }

    private static class ExtractToFile implements BodyExtractor<Mono<File>, ReactiveHttpInputMessage> {
        @Override
        public Mono<File> extract(ReactiveHttpInputMessage inputMessage,
                BodyExtractor.Context context) {
            return Mono.create(emitter -> {
                try {
                    File file = File.createTempFile("downloaded", ".jar");
                    AtomicInteger size = new AtomicInteger(0);
                    AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE);
                    inputMessage.getBody()
                    .doFinally(signal -> {
                        if (channel.isOpen()) try {
                            channel.close();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .map(DataBuffer::asByteBuffer)
                    .flatMap(buffer -> {
                        int position = size.getAndAdd(buffer.capacity());
                        CompletableFuture<Integer> future = makeCompletableFuture(channel.write(buffer, position));
                        return Mono.fromFuture(future);
                    })
                    .reduce((a, b) -> a + b)
                    .doOnNext(total -> {
                        log.info("Downloaded {} bytes", total);
                    })
                    .subscribe(total -> {
                        if (channel.isOpen()) try {
                            channel.close();
                        } catch (IOException e) {
                            emitter.error(e);
                        }
                        emitter.success(file);
                    }, emitter::error);
                } catch (RuntimeException | IOException e) {
                    emitter.error(e);
                }
            });
        }

        // https://stackoverflow.com/a/23302308/814928
        private <T> CompletableFuture<T> makeCompletableFuture(Future<T> future) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
