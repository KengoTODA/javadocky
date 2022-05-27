package jp.skypencil.javadocky.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import reactor.test.StepVerifier
import java.io.File
import java.util.*

class JavadocDownloaderTest : FunSpec({
    val root = tempdir()

    test("download an existing javadoc") {
        val downloaded = JavadocDownloader(root.toPath(), MAVEN_REPO)
            .download("com.github.spotbugs", "spotbugs", "3.1.0-RC3")
        StepVerifier.create(downloaded)
            .expectNextMatches { optional: Optional<File> ->
                optional.get().length() == 7268250L
            }
            .expectComplete()
            .verify()
    }

    test("download missing javadoc") {
        val downloaded = JavadocDownloader(root.toPath(), MAVEN_REPO)
            .download("com.github.spotbugs", "spotbugs", "3.1.0-RC0")
        StepVerifier.create(downloaded)
            .expectNextMatches { optional: Optional<File> -> !optional.isPresent }
            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private const val MAVEN_REPO = "https://repo.maven.apache.org/maven2/"
    }
}
