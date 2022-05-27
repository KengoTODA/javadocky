package jp.skypencil.javadocky.page

import static org.springframework.web.reactive.function.server.RequestPredicates.GET
import static org.springframework.web.reactive.function.server.RouterFunctions.route
import static org.springframework.web.reactive.function.server.ServerResponse.notFound
import static org.springframework.web.reactive.function.server.ServerResponse.ok
import static org.springframework.web.reactive.function.server.ServerResponse.seeOther

import java.io.File
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import javax.annotation.ParametersAreNonnullByDefault
import jp.skypencil.javadocky.repository.Storage
import jp.skypencil.javadocky.repository.VersionRepository
import jp.skypencil.javadocky.service.JavadocExtractor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.io.FileSystemResource
import org.springframework.http.CacheControl
import org.springframework.lang.NonNull
import org.springframework.stereotype.Controller
import org.springframework.util.AntPathMatcher
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

object PageController {
  const URL_PATTERN = "/page/{groupId}/{artifactId}/{version}/**"
  const FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
}

@Controller
class PageController {
  private val log: Logger = LoggerFactory.getLogger(getClass())
  private val storage: Storage
  private val extractor: JavadocExtractor
  private val versionRepo: VersionRepository

  @Autowired
  @ParametersAreNonnullByDefault
  PageController(storage: Storage, extractor: JavadocExtractor, versionRepo: VersionRepository) {
    this.storage = Objects.requireNonNull(storage);
    this.extractor = Objects.requireNonNull(extractor);
    this.versionRepo = Objects.requireNonNull(versionRepo);
  }

  @Bean
  fun routeForPage(): RouterFunction<ServerResponse> {
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

  private fun findFilePath(req: ServerRequest): String {
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
  private fun findRawFilePath(req: ServerRequest): String {
    return new AntPathMatcher().extractPathWithinPattern(URL_PATTERN, req.path());
  }

  private fun response(
    groupId: String, artifactId: String, version: String, path: String): Mono<ServerResponse> {
    Mono<File> extract = extractor.extract(groupId, artifactId, version, path);
    return storage
        .find(groupId, artifactId, version, path)
        .switchIfEmpty(
            extract.doOnSubscribe(
                subscription ->
                    log.info(
                        "{} for {}:{}:{} not found, try to unzip",
                        path,
                        groupId,
                        artifactId,
                        version)))
        .flatMap(
            file -> {
              log.trace("Requested file found at {}", file.getAbsolutePath());
              ZonedDateTime lastModified =
                  ZonedDateTime.ofInstant(
                      Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
              ZonedDateTime expired =
                  // according to RFC7234, 1 year is max value for Expires
                  ZonedDateTime.now(ZoneId.systemDefault()).plusYears(1L);
              return ok().header("Last-Modifed", lastModified.format(FORMAT))
                  // all data are immutable, does not depend on session state
                  .header("Cache-Control", "public")
                  .header("Expires", expired.format(FORMAT))
                  .body(Mono.just(new FileSystemResource(file)), FileSystemResource.class);
            });
  }
}
