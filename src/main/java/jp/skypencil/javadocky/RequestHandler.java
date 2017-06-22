package jp.skypencil.javadocky;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
class RequestHandler {
    @NonNull
    private final Storage storage;

    @NonNull
    private final JavadocExtractor extractor;

    Mono<ServerResponse> response(String groupId, String artifactId, String version, String path) {
        return storage.find(groupId, artifactId, version, path).flatMap(file -> {
            if (file.isPresent()) {
                log.trace("Requested file found at {}", file.get());
                return ok().body(Mono.just(new FileSystemResource(file.get())), FileSystemResource.class);
            } else {
                log.info("Javadoc for {}:{}:{} not found, kick a job to download & unzip them", groupId, artifactId, version);
                extractor.extract(groupId, artifactId, version).subscribe();
                return ok().body(Mono.just("File not found, trying to download.."), String.class);
            }
        });
    }}
