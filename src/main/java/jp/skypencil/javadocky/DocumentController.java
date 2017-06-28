package jp.skypencil.javadocky;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.seeOther;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
class DocumentController {
    private static final String URL_PATTERN = "/doc/{groupId}/{artifactId}/{version}/**";

    private static final String URL_PATTERN_FOR_LATEST = "/doc/{groupId}/{artifactId}/latest/**";

    private static final String URL_PATTERN_WITHOUT_VERSION = "/doc/{groupId}/{artifactId}/";

    @NonNull
    private final Storage storage;

    @NonNull
    private final JavadocExtractor extractor;

    @NonNull
    private final VersionRepository versionRepo;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");

    @Bean
    public RouterFunction<ServerResponse> routeForLatestPage(DocumentController requestHandler) {
        return route(GET(DocumentController.URL_PATTERN_FOR_LATEST), req -> {
            String groupId = req.pathVariable("groupId");
            String artifactId = req.pathVariable("artifactId");
            String rawPath = findRawFilePath(req);
            log.debug("Got access to latest page {}:{} with path {}", groupId, artifactId, rawPath);

            return versionRepo.findLatest(groupId, artifactId).flatMap(optionalVersion -> {
                if (!optionalVersion.isPresent()) {
                    return notFound().build();
                }
                ArtifactVersion latestVersion = optionalVersion.get();
                URI uri = URI.create("/doc/").resolve(groupId + "/").resolve(artifactId + "/").resolve(latestVersion.toString() + "/").resolve(rawPath);
                log.info("Latest version for {}:{} is {}, redirecting to {}",
                        groupId, artifactId, latestVersion, uri);
                return seeOther(uri).build();
            });
        });
    }

    /**
     * <p>When client access to artifact page ({@code /doc/{groupId}/{artifactId}/},
     * try to find latest version and redirect to its top page.</p>
     * @param requestHandler non-null request handler
     * @return router function for artifact page
     */
    @Bean
    public RouterFunction<ServerResponse> routeForArtifact(DocumentController requestHandler) {
        return route(GET(DocumentController.URL_PATTERN_WITHOUT_VERSION),
                req -> {
                    String groupId = req.pathVariable("groupId");
                    String artifactId = req.pathVariable("artifactId");
                    log.debug("Got access to artifact page {}:{}", groupId, artifactId);

                    return versionRepo.findLatest(groupId, artifactId).flatMap(optionalVersion -> {
                        if (!optionalVersion.isPresent()) {
                            return notFound().cacheControl(CacheControl.noStore()).build();
                        }
                        ArtifactVersion latestVersion = optionalVersion.get();
                        URI uri = URI.create("/doc/").resolve(groupId + "/").resolve(artifactId + "/").resolve(latestVersion.toString() + "/");
                        log.info("Latest version for {}:{} is {}, redirecting to {}",
                                groupId, artifactId, latestVersion, uri);
                        return seeOther(uri).build();
                    });
                });
    }

    @Bean
    public RouterFunction<ServerResponse> routeForPage(DocumentController requestHandler) {
        return route(GET(DocumentController.URL_PATTERN), req -> {
            String groupId = req.pathVariable("groupId");
            String artifactId = req.pathVariable("artifactId");
            String path = findFilePath(req);
            String version = req.pathVariable("version");
            if (version.equals("latest")) {
                throw new IllegalArgumentException("access for latest version should be handled by #routeForLatestPage(RequestHander) method");
            }
            return response(groupId, artifactId, version, path, false);
        });
    }

    private String findFilePath(ServerRequest req) {
        String path = findRawFilePath(req);
        if (path == null || path.isEmpty()) {
            path = "index.html";
        }
        return path;
    }

    /**
     * @see <a href=
     *      "https://stackoverflow.com/questions/3686808/spring-3-requestmapping-get-path-value">related
     *      SO post</a>
     */
    private String findRawFilePath(ServerRequest req) {
        return new AntPathMatcher().extractPathWithinPattern(URL_PATTERN, req.path());
    }

    private Mono<ServerResponse> response(String groupId, String artifactId, String version, String path, boolean shortlyExpired) {
        return storage.find(groupId, artifactId, version, path).flatMap(file -> {
            if (!file.isPresent()) {
                log.info("{} for {}:{}:{} not found, try to unzip", path, groupId, artifactId, version);
                return extractor.extract(groupId, artifactId, version, path);
            }
            return Mono.just(file.get());
        }).flatMap(file -> {
                log.trace("Requested file found at {}", file.getAbsolutePath());
                ZonedDateTime lastModified = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
                ZonedDateTime expired = shortlyExpired
                        ? ZonedDateTime.now().plusDays(1L)
                        // according to RFC7234, 1 year is max value for Expires
                        : ZonedDateTime.now().plusYears(1L);
                return ok()
                        .header("Last-Modifed", lastModified.format(FORMAT))
                        // all data are immutable, does not depend on session state
                        .header("Cache-Control", "public")
                        .header("Expires", expired.format(FORMAT))
                        .body(Mono.just(new FileSystemResource(file)), FileSystemResource.class);
        });
    }}
