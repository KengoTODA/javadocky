package jp.skypencil.javadocky.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class LocalStorageTest : FunSpec({
    val root = tempdir()

    test("empty directory returns null") {
        val storage: Storage = LocalStorage(root.toPath())
        storage.find("g", "a", "v", "index.html").block() shouldBe null
    }

    test("written storage returns an entry") {
        val storage: Storage = LocalStorage(root.toPath())
        val factory: DataBufferFactory = DefaultDataBufferFactory()
        storage
            .write(
                "g",
                "a",
                "v",
                "index.html",
                Flux.just(factory.wrap("Hello world!".toByteArray(StandardCharsets.UTF_8)))
            )
            .block()

        val written = storage.find("g", "a", "v", "index.html").block()
        written shouldNotBe null
        Files.readAllLines(written!!.toPath()) shouldContainExactly listOf("Hello world!")
    }
})
