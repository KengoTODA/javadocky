package jp.skypencil.javadocky

import jp.skypencil.javadocky.repository.ArtifactRepository
import jp.skypencil.javadocky.repository.LocalStorage
import jp.skypencil.javadocky.repository.LocalStorageArtifactRepository
import jp.skypencil.javadocky.repository.Storage
import jp.skypencil.javadocky.repository.VersionRepository
import jp.skypencil.javadocky.service.JavadocDownloader
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.file.Paths
import java.util.*

@SpringBootApplication
open class JavadockyApplication {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    open fun localStorage(): Storage {
        val home = Paths.get(System.getProperty(USER_HOME), JAVADOCKY_ROOT, STORAGE_DIR)
        home.toFile().mkdirs()
        log.info("Making storage at {}", home.toFile().absolutePath)
        return LocalStorage(home)
    }

    @Bean
    open fun artifactRepository(): ArtifactRepository {
        val home = Paths.get(System.getProperty(USER_HOME), JAVADOCKY_ROOT, JAVADOC_DIR)
        home.toFile().mkdirs()
        log.info("Making storage at {}", home.toFile().absolutePath)
        return LocalStorageArtifactRepository(home)
    }

    @Bean
    open fun javadocDownloader(
        @Value("\${javadocky.maven.repository}") repoURL: String
    ): JavadocDownloader {
        val home = Paths.get(System.getProperty(USER_HOME), JAVADOCKY_ROOT, JAVADOC_DIR)
        home.toFile().mkdirs()
        log.info("Making javadoc storage at {}", home.toFile().absolutePath)
        return JavadocDownloader(home, repoURL)
    }

    @Bean
    open fun routes(artifactRepo: ArtifactRepository, versionRepo: VersionRepository) = router {
        GET("/doc/{groupId}/{artifactId}") { req: ServerRequest ->
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
        GET("/badge/{groupId}/{artifactId}.{ext}") { req: ServerRequest ->
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

    companion object {
        private const val USER_HOME = "user.home"
        private const val JAVADOCKY_ROOT = ".javadocky"
        private const val STORAGE_DIR = "storage"

        /** Name of directory to store downloaded javadoc.jar file.  */
        private const val JAVADOC_DIR = "javadoc"
        @JvmStatic
        fun main(args: Array<String>) {
            val isAppCds = args.contains("--appcds")
            val port = System.getenv("PORT")
            val app = SpringApplication(JavadockyApplication::class.java)
            if (port != null) {
                // for Heroku, respect the given PORT environment variable
                app.setDefaultProperties(Collections.singletonMap<String, Any>("server.port", port))
            }
            @Suppress("SpreadOperator")
            val ctx = app.run(*args)

            // TODO consider the best timing to stop the process
            if (isAppCds) {
                System.err.println("Beans construction complete, so going to exit the process")
                ctx.close()
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
