package jp.skypencil.javadocky;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

class JavadocDownloader {
    private static final String REPO_URL = "http://central.maven.org/maven2/";
    private WebClient webClient = WebClient.create(REPO_URL);

    Mono<File> download(String groupId, String artifactId, String version) {
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

    private Mono<File> fetchResponse(ClientResponse res) {
        if (!res.statusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Unexpected status code:" + res.statusCode().value());
        }
        return res.body(new ExtractToFile());
    }

    private static class ExtractToFile implements BodyExtractor<Mono<File>, ReactiveHttpInputMessage> {
        @Override
        public Mono<File> extract(ReactiveHttpInputMessage inputMessage,
                BodyExtractor.Context context) {
            return Mono.fromDirect(source -> {
                try {
                    File file = File.createTempFile("downloaded", ".jar");
                    SeekableByteChannel channel = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE);
                    inputMessage.getBody()
                    .doFinally(signal -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .map(DataBuffer::asByteBuffer)
                    .map(buffer -> {
                        try {
                            return channel.write(buffer);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .reduce((a, b) -> a + b)
                    .subscribe(total -> {
                        source.onNext(file);
                    });
                } catch (RuntimeException | IOException e) {
                    source.onError(e);
                }
            });
        }
    }
}
