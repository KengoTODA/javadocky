package jp.skypencil.javadocky.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File
import reactor.test.StepVerifier

class LocalStorageArtifactRepositoryTest : FunSpec({
    val root = tempdir()

    test("without dir") {
        val repo = LocalStorageArtifactRepository(root.toPath())
        StepVerifier.create(repo.list("foo")).expectComplete().verify()
    }

    test("with an empty dir") {
        val repo = LocalStorageArtifactRepository(root.toPath())
        File(root, "foo").mkdir() shouldBe true
        StepVerifier.create(repo.list("foo")).expectComplete().verify()
    }

    test("with a directory that has files in it") {
        val repo = LocalStorageArtifactRepository(root.toPath())
        val groupDir = File(root, "group")
        groupDir.mkdir() shouldBe true
        File(groupDir, "artifact-1").mkdir() shouldBe true
        File(groupDir, "artifact-2").mkdir() shouldBe true
        StepVerifier.create(repo.list("group"))
            .expectNext("artifact-1")
            .expectNext("artifact-2")
            .expectComplete()
            .verify()
    }
})
