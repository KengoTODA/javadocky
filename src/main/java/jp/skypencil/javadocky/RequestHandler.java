package jp.skypencil.javadocky;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
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

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");

    Mono<ServerResponse> response(String groupId, String artifactId, String version, String path) {
        return storage.find(groupId, artifactId, version, path).flatMap(file -> {
            if (file.isPresent()) {
                log.trace("Requested file found at {}", file.get());
                ZonedDateTime lastModified = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.get().lastModified()), ZoneId.systemDefault());
                return ok()
                        .header("Last-Modifed", lastModified.format(FORMAT))
                        // all data are immutable, does not depend on session state
                        .header("Cache-Control", "public")
                        // according to RFC7234, 1 year is max value for Expires
                        .header("Expires", ZonedDateTime.now().plusYears(1L).format(FORMAT))
                        .body(Mono.just(new FileSystemResource(file.get())), FileSystemResource.class);
            } else {
                log.info("Javadoc for {}:{}:{} not found, kick a job to download & unzip them", groupId, artifactId, version);
                extractor.extract(groupId, artifactId, version).subscribe();
                return status(HttpStatus.NOT_FOUND).body(Mono.just("File not found, trying to download.."), String.class);
            }
        });
    }}
