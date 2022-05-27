package jp.skypencil.javadocky.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import reactor.test.StepVerifier
import java.io.File

class LocalStorageVersionRepositoryTest : FunSpec({
    val root = tempdir()
    test("returns nothing if there is no directory") {
        val repository = LocalStorageVersionRepository(root.toPath())
        StepVerifier.create(repository.findLatest("g", "a")).expectComplete().verify()
    }

    test("always return the latest version") {
        val repository = LocalStorageVersionRepository(root.toPath())
        val groupDir = File(root, "g")
        val artifactDir = File(groupDir, "a")
        artifactDir.mkdirs() shouldBe true

        File(artifactDir, "1.0.1").mkdir() shouldBe true
        StepVerifier.create(repository.findLatest("g", "a"))
            .expectNext(DefaultArtifactVersion("1.0.1"))
            .expectComplete()
            .verify()

        File(artifactDir, "1.0.0").mkdir() shouldBe true
        StepVerifier.create(repository.findLatest("g", "a"))
            .expectNext(DefaultArtifactVersion("1.0.1"))
            .expectComplete()
            .verify()

        File(artifactDir, "1.0.2").mkdir() shouldBe true
        StepVerifier.create(repository.findLatest("g", "a"))
            .expectNext(DefaultArtifactVersion("1.0.2"))
            .expectComplete()
            .verify()

        File(artifactDir, "1.1.0").mkdir() shouldBe true
        StepVerifier.create(repository.findLatest("g", "a"))
            .expectNext(DefaultArtifactVersion("1.1.0"))
            .expectComplete()
            .verify()

        File(artifactDir, "1.1.0-SNAPSHOT").mkdir() shouldBe true
        StepVerifier.create(repository.findLatest("g", "a"))
            .expectNext(DefaultArtifactVersion("1.1.0"))
            .expectComplete()
            .verify()

        File(artifactDir, "1.1.0-sp1").mkdir() shouldBe true
        StepVerifier.create(repository.findLatest("g", "a"))
            .expectNext(DefaultArtifactVersion("1.1.0-sp1"))
            .expectComplete()
            .verify()
    }
})
