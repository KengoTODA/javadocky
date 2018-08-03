package jp.skypencil.javadocky.controller;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.seeOther;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import jp.skypencil.javadocky.JavadocExtractor;
import jp.skypencil.javadocky.Storage;
import jp.skypencil.javadocky.VersionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class PageController {
  private static final String URL_PATTERN = "/page/{groupId}/{artifactId}/{version}/**";

  private final Logger log = LoggerFactory.getLogger(getClass());

  @NonNull private final Storage storage;

  @NonNull private final JavadocExtractor extractor;

  @NonNull private final VersionRepository versionRepo;

  private static final DateTimeFormatter FORMAT =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");

  @Bean
  public RouterFunction<ServerResponse> routeForPage() {
    return route(
        GET(PageController.URL_PATTERN),
        req -> {
          String groupId = req.pathVariable("groupId");
          String artifactId = req.pathVariable("artifactId");
          String path = findFilePath(req);
          String version = req.pathVariable("version");
          if ("latest".equals(version)) {
            return versionRepo
                .findLatest(groupId, artifactId)
                .flatMap(
                    latestVersion -> {
                      URI uri =
                          URI.create("/page/")
                              .resolve(groupId + "/")
                              .resolve(artifactId + "/")
                              .resolve(latestVersion.toString() + "/");
                      log.info(
                          "Latest version for {}:{} is {}, redirecting to {}",
                          groupId,
                          artifactId,
                          latestVersion,
                          uri);
                      return seeOther(uri).build();
                    })
                .switchIfEmpty(notFound().cacheControl(CacheControl.noStore()).build());
          } else {
            // TODO support major_version
          }
          return response(groupId, artifactId, version, path);
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
   *     "https://stackoverflow.com/questions/3686808/spring-3-requestmapping-get-path-value">related
   *     SO post</a>
   */
  private String findRawFilePath(ServerRequest req) {
    return new AntPathMatcher().extractPathWithinPattern(URL_PATTERN, req.path());
  }

  private Mono<ServerResponse> response(
      String groupId, String artifactId, String version, String path) {
    Mono<File> extract = extractor.extract(groupId, artifactId, version, path);
    return storage
        .find(groupId, artifactId, version, path)
        .switchIfEmpty(
            extract.doOnSubscribe(
                subscription -> {
                  log.info(
                      "{} for {}:{}:{} not found, try to unzip",
                      path,
                      groupId,
                      artifactId,
                      version);
                }))
        .flatMap(
            file -> {
              log.trace("Requested file found at {}", file.getAbsolutePath());
              ZonedDateTime lastModified =
                  ZonedDateTime.ofInstant(
                      Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
              ZonedDateTime expired =
                  // according to RFC7234, 1 year is max value for Expires
                  ZonedDateTime.now().plusYears(1L);
              return ok().header("Last-Modifed", lastModified.format(FORMAT))
                  // all data are immutable, does not depend on session state
                  .header("Cache-Control", "public")
                  .header("Expires", expired.format(FORMAT))
                  .body(Mono.just(new FileSystemResource(file)), FileSystemResource.class);
            });
  }
}
