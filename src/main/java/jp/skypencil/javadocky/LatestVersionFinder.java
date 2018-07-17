package jp.skypencil.javadocky;

import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class LatestVersionFinder {
  private boolean catchNext;

  String parse(String result, XMLEvent xml) {
    if (catchNext && xml.isCharacters()) {
      result = ((Characters) xml).getData();
    }
    catchNext = xml.isStartElement() && "latest".equals(((StartElement) xml).getName().toString());
    return result;
  }
}
