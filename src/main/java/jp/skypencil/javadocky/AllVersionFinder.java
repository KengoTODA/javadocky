package jp.skypencil.javadocky;

import java.util.Set;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

class AllVersionFinder {
  private boolean catchNext;

  Set<DefaultArtifactVersion> parse(Set<DefaultArtifactVersion> result, XMLEvent xml) {
    if (catchNext && xml.isCharacters()) {
      result.add(new DefaultArtifactVersion(((Characters) xml).getData()));
    }
    catchNext = xml.isStartElement() && "version".equals(((StartElement) xml).getName().toString());
    return result;
  }
}
