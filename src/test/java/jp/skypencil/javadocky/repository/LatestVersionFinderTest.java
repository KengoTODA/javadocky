package jp.skypencil.javadocky.repository;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.xml.stream.events.XMLEvent;
import org.junit.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.xml.XmlEventDecoder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class LatestVersionFinderTest {
  private static final String XML =
      "<metadata>"
          + "<groupId>com.github.spotbugs</groupId>"
          + "<artifactId>spotbugs</artifactId>"
          + "<versioning>"
          + "<latest>3.1.0-RC3</latest>"
          + "<release>3.1.0-RC3</release>"
          + "<versions>"
          + "<version>3.1.0-RC1</version>"
          + "<version>3.1.0-RC2</version>"
          + "<version>3.1.0-RC3</version>"
          + "</versions>"
          + "<lastUpdated>20170610023153</lastUpdated>"
          + "</versioning>"
          + "</metadata>";

  @Test
  public void test() {
    Flux<XMLEvent> events =
        new XmlEventDecoder()
            .decode(Flux.just(stringBuffer(XML)), null, null, Collections.emptyMap());
    LatestVersionFinder finder = new LatestVersionFinder();
    StepVerifier.create(events.reduce("", finder::parse))
        .expectNext("3.1.0-RC3")
        .expectComplete()
        .verify();
  }

  private DataBuffer stringBuffer(String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = new DefaultDataBufferFactory().allocateBuffer(bytes.length);
    buffer.write(bytes);
    return buffer;
  }
}
