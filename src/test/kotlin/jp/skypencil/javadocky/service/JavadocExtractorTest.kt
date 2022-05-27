package jp.skypencil.javadocky.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import jp.skypencil.javadocky.repository.LocalStorage
import jp.skypencil.javadocky.repository.Storage
import reactor.test.StepVerifier
import java.io.File

class JavadocExtractorTest : FunSpec({
    val root = tempdir()
    val javadoc = tempdir()

    test("extract a javadoc into storage") {
        val downloader = JavadocDownloader(javadoc.toPath(), MAVEN_REPO)
        val storage: Storage = LocalStorage(root.toPath())
        val extractor = JavadocExtractor(downloader, storage)

        val downloaded = extractor
            .extract("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html")
            .block()
        downloaded!!.isFile shouldBe true

        StepVerifier.create(
            storage.find("com.github.spotbugs", "spotbugs-annotations", "3.1.0-RC3", "index.html")
        )
            .expectNextMatches { file: File -> file == downloaded }
            .expectComplete()
            .verify()
    }
}) {

    companion object {
        private const val MAVEN_REPO = "https://repo.maven.apache.org/maven2/"
    }
}
