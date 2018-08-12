package jp.skypencil.javadocky.controller;

import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.seeOther;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

import javax.validation.Valid;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;

import jp.skypencil.javadocky.ArtifactName;
import jp.skypencil.javadocky.repository.VersionRepository;
import reactor.core.publisher.Mono;

@RestController
class BadgeController {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @NonNull private final VersionRepository versionRepo;

  @Autowired
  BadgeController(@NonNull VersionRepository versionRepo) {
    this.versionRepo = Objects.requireNonNull(versionRepo);
  }

  @GetMapping("/badge/{groupId}/{artifactId}.{ext}")
  public Mono<ServerResponse> badge(
      @ModelAttribute @Valid ArtifactName artifactName,
      @PathVariable String ext,
      @RequestParam(name = "color", required = false) String color,
      @RequestParam(name = "label", required = false) String label) {
    if (!ext.equals("png") && !ext.equals("svg")) {
      return badRequest().body(Mono.just("Unsupported extention"), String.class);
    }

    String groupId = artifactName.getGroupId();
    String artifactId = artifactName.getArtifactId();
    log.debug("Got access to badge for {}", artifactName);
    return versionRepo
        .findLatest(groupId, artifactId)
        .flatMap(redirect(label, color, ext))
        .switchIfEmpty(notFound().build());
  }

  private Function<ArtifactVersion, Mono<ServerResponse>> redirect(
      @Nullable String label, @Nullable String color, @NonNull String ext) {
    return latestVersion -> {
      URI shieldsUri =
          URI.create(
              String.format(
                  "https://img.shields.io/badge/%s-%s-%s.%s",
                  escape(label != null ? label : "javadoc"),
                  escape(latestVersion.toString()),
                  escape(color != null ? color : "brightgreen"),
                  ext));
      return seeOther(shieldsUri).build();
    };
  }

  /**
   * Escape URI based on the rule described by <a href="https://shields.io/">shields.io</a>
   *
   * @param s target string
   * @return escaped string
   */
  private String escape(String s) {
    return s.replace("-", "--").replace("_", "__").replace(" ", "_");
  }

  @ModelAttribute
  ArtifactName createArtifactName(@PathVariable String groupId, @PathVariable String artifactId) {
    return new ArtifactName(groupId, artifactId);
  }
}
