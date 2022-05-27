package jp.skypencil.javadocky.repository

import io.kotest.core.spec.style.FunSpec
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.xml.XmlEventDecoder
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import javax.xml.stream.events.XMLEvent

fun String.toDataBuffer(): DataBuffer {
    val bytes: ByteArray = this.toByteArray(java.nio.charset.StandardCharsets.UTF_8)
    val buffer: DataBuffer =
        org.springframework.core.io.buffer.DefaultDataBufferFactory().allocateBuffer(bytes.size)
    buffer.write(bytes)
    return buffer
}

class LatestVersionFinderTest : FunSpec({
    test("LatestVersionFinder returns the value of the <latest> element in the given XML") {
        val events: Flux<XMLEvent> = XmlEventDecoder()
            .decode(
                Flux.just<DataBuffer>(XML.toDataBuffer()),
                null,
                null,
                emptyMap<String, Any>()
            )
        val finder = LatestVersionFinder()
        StepVerifier.create<String>(
            events.reduce<String>(
                "",
                java.util.function.BiFunction<String, XMLEvent, String> { result: String?, xml: javax.xml.stream.events.XMLEvent? ->
                    finder.parse(
                        result,
                        xml
                    )
                }
            )
        )
            .expectNext("3.1.0-RC3")
            .expectComplete()
            .verify()
    }
}) {

    companion object {
        private const val XML = (
            "<metadata>" +
                "<groupId>com.github.spotbugs</groupId>" +
                "<artifactId>spotbugs</artifactId>" +
                "<versioning>" +
                "<latest>3.1.0-RC3</latest>" +
                "<release>3.1.0-RC3</release>" +
                "<versions>" +
                "<version>3.1.0-RC1</version>" +
                "<version>3.1.0-RC2</version>" +
                "<version>3.1.0-RC3</version>" +
                "</versions>" +
                "<lastUpdated>20170610023153</lastUpdated>" +
                "</versioning>" +
                "</metadata>"
            )
    }
}
