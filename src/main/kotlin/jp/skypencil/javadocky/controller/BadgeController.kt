package jp.skypencil.javadocky.controller

import jp.skypencil.javadocky.repository.VersionRepository
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

@Controller
internal class BadgeController @Autowired constructor(private val versionRepo: VersionRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun routeForBadge(): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET("/badge/{groupId}/{artifactId}.{ext}")
        ) { req: ServerRequest ->
            val ext = req.pathVariable("ext")
            if (ext != "png" && ext != "svg") {
                ServerResponse.badRequest()
                    .body(
                        Mono.just("Unsupported extension"),
                        String::class.java
                    )
            } else {
                val groupId = req.pathVariable("groupId")
                val artifactId = req.pathVariable("artifactId")
                log.debug("Got access to badge for {}:{}", groupId, artifactId)
                versionRepo
                    .findLatest(groupId, artifactId)
                    .flatMap { latestVersion: ArtifactVersion ->
                        val shieldsUri = URI.create(
                            String.format(
                                Locale.getDefault(),
                                "https://img.shields.io/badge/%s-%s-%s.%s",
                                escape(req.queryParam("label").orElse("javadoc")),
                                escape(latestVersion.toString()),
                                escape(req.queryParam("color").orElse("brightgreen")),
                                ext
                            )
                        )
                        ServerResponse.seeOther(shieldsUri).build()
                    }
                    .switchIfEmpty(ServerResponse.notFound().build())
            }
        }
    }

    /**
     * @param s target string
     * @return An escaped string based on the rule described by [shields.io](https://shields.io/)
     */
    private fun escape(s: String): String {
        return s.replace("-", "--").replace("_", "__").replace(" ", "_")
    }
}
