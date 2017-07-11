package jp.skypencil.javadocky;

import java.net.URI;
import java.util.Collections;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>A {@link VersionRepository} implementation which refers XML in remote Maven repository.</p>
 */
@Slf4j
@Repository
class RemoteRepoVersionReposiory implements VersionRepository {
    private static final String REPO_URL = "http://central.maven.org/maven2/";
    private static final String XML_NAME = "maven-metadata.xml";

    private WebClient webClient = WebClient.create(REPO_URL);

    @Override
    public Mono<? extends ArtifactVersion> findLatest(String groupId, String artifactId) {
        URI uri = URI.create(REPO_URL).resolve(groupId.replace('.', '/') + "/").resolve(artifactId + "/").resolve(XML_NAME);
        log.info("Downloading metadata from {}", uri);
         Mono<ClientResponse> response = webClient.get()
            .uri(String.format("%s/%s/" + XML_NAME, groupId.replace('.', '/'), artifactId))
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
        // XXX -Dio.netty.buffer.bytebuf.checkAccessible=false is necessary, or we face IllegalReferenceCountException(refCnt = 0) in AbstractByteBuf.class
        // https://jira.spring.io/browse/SPR-15707
        return new XmlEventDecoder()
                .decode(data, null, null, Collections.emptyMap())
                .reduce("", finder::parse)
                .filter(s -> !s.isEmpty())
                .map(DefaultArtifactVersion::new);
    }
}
