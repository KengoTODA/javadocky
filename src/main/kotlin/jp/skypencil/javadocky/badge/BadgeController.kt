package jp.skypencil.javadocky.controller

import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.seeOther

import java.net.URI
import java.util.Objects
import jp.skypencil.javadocky.repository.VersionRepository
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.lang.NonNull
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Controller
class BadgeController {
    private val log: Logger = LoggerFactory.getLogger(getClass());
    private val versionRepo: VersionRepository;

    @Autowired
    BadgeController(versionRepo: VersionRepository)
    {
        this.versionRepo = Objects.requireNonNull(versionRepo);
    }

    @Bean
    fun routeForBadge(): RouterFunction<ServerResponse>
    {
        return route(
            GET("/badge/{groupId}/{artifactId}.{ext}"),
            req -> {
        val ext = req.pathVariable("ext");
        if (!ext.equals("png") && !ext.equals("svg")) {
            return badRequest().body(Mono.just("Unsupported extention"), String.class);
        }

        val groupId = req.pathVariable("groupId");
        val artifactId = req.pathVariable("artifactId");
        log.debug("Got access to badge for {}:{}", groupId, artifactId);

        return versionRepo
            .findLatest(groupId, artifactId)
            .flatMap({ latestVersion: ArtifactVersion ->
                URI shieldsUri =
                URI.create(
                    String.format(
                        "https://img.shields.io/badge/%s-%s-%s.%s",
                        escape(req.queryParam("label").orElse("javadoc")),
                        escape(latestVersion.toString()),
                        escape(req.queryParam("color").orElse("brightgreen")),
                        ext
                    )
                );
                return seeOther(shieldsUri).build();
            })
            .switchIfEmpty(notFound().build());
    });
    }

    /**
     * Escape URI based on the rule described by <a href="https://shields.io/">shields.io</a>
     *
     * @param s target string
     * @return escaped string
     */
    private fun escape(s: String): String {
        return s.replace("-", "--").replace("_", "__").replace(" ", "_");
    }
}
