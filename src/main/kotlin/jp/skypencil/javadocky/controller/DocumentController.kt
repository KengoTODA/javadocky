package jp.skypencil.javadocky.controller

import jp.skypencil.javadocky.repository.ArtifactRepository
import jp.skypencil.javadocky.repository.VersionRepository
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux

@Controller
internal class DocumentController @Autowired constructor(
    private val versionRepo: VersionRepository,
    private val artifactRepo: ArtifactRepository
) {

    @Bean
    fun routeForDoc(): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET("/doc/{groupId}/{artifactId}")
        ) { req: ServerRequest ->
            val groupId = req.pathVariable("groupId")
            val artifactId = req.pathVariable("artifactId")
            val artifacts = artifactRepo.list(groupId)
            val versions: Flux<out ArtifactVersion> = versionRepo.list(groupId, artifactId)
            versionRepo
                .findLatest(groupId, artifactId)
                .flatMap { latestVersion: ArtifactVersion ->
                    val model = mapOf(
                        "groupId" to groupId,
                        "artifactId" to artifactId,
                        "artifactIds" to artifacts,
                        "version" to latestVersion,
                        "versions" to versions,
                    )
                    ServerResponse.ok().contentType(MediaType.TEXT_HTML)
                        .render("doc", model)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
        }
    }
}
