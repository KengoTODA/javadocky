package jp.skypencil.javadocky.repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** A {@link VersionRepository} implementation which refers XML in remote Maven repository. */
@Repository
class RemoteRepoVersionReposiory implements VersionRepository {
  private static final String XML_NAME = "maven-metadata.xml";

  private final WebClient webClient;

  @Autowired
  RemoteRepoVersionReposiory(@NonNull @Value("${javadocky.maven.repository}") String repoURL) {
    this.webClient = WebClient.create(repoURL);
  }

  @Override
  public Mono<ArtifactVersion> findLatest(String groupId, String artifactId) {
    Mono<ClientResponse> response =
        webClient
            .get()
            .uri(String.format("%s/%s/%s", groupId.replace('.', '/'), artifactId, XML_NAME))
            .accept(MediaType.TEXT_XML, MediaType.APPLICATION_XML)
            .exchange();
    return response.flatMap(this::fetchResponse);
  }

  private Mono<ArtifactVersion> fetchResponse(ClientResponse res) {
    HttpStatus status = res.statusCode();
    if (status == HttpStatus.NOT_FOUND) {
      return Mono.empty();
    } else if (!status.is2xxSuccessful()) {
      return Mono.error(new IllegalArgumentException("Unexpected status code:" + status.value()));
    }

    Flux<DataBuffer> data = res.body(BodyExtractors.toDataBuffers());
    LatestVersionFinder finder = new LatestVersionFinder();
    return new XmlEventDecoder()
        .decode(data, null, null, Collections.emptyMap())
        .reduce("", finder::parse)
        .filter(s -> !s.isEmpty())
        .map(DefaultArtifactVersion::new);
  }

  @Override
  public Flux<ArtifactVersion> list(String groupId, String artifactId) {
    Mono<ClientResponse> response =
        webClient
            .get()
            .uri(String.format("%s/%s/%s", groupId.replace('.', '/'), artifactId, XML_NAME))
            .accept(MediaType.TEXT_XML, MediaType.APPLICATION_XML)
            .exchange();
    return response.flatMapMany(this::listVersions);
  }

  private Flux<ArtifactVersion> listVersions(ClientResponse res) {
    HttpStatus status = res.statusCode();
    if (status == HttpStatus.NOT_FOUND) {
      return Flux.empty();
    } else if (!status.is2xxSuccessful()) {
      return Flux.error(new IllegalArgumentException("Unexpected status code:" + status.value()));
    }

    Flux<DataBuffer> data = res.body(BodyExtractors.toDataBuffers());
    AllVersionFinder finder = new AllVersionFinder();
    return new XmlEventDecoder()
        .decode(data, null, null, Collections.emptyMap())
        .reduce(new HashSet<DefaultArtifactVersion>(), finder::parse)
        .flatMapIterable(set -> set)
        .sort(Comparator.reverseOrder())
        .cast(ArtifactVersion.class);
  }
}
