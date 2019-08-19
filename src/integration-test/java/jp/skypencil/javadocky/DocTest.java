package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.element;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocTest {
  @LocalServerPort private int port;

  @Rule public BrowserStack browserStack = new BrowserStack();

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  public void testDocPageShouldHaveIframe() {
    open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    element(By.tagName("iframe")).should(exist);
  }

  /** Doc page should have dropdown list to select {@code artifactId}. */
  @Test
  public void testDocPageShouldHaveListOfArtifactId() {
    open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    $("li.dropdown#artifact-id").shouldHave(text("helper"));
  }

  /** Doc page should have dropdown list to select {@code version}. */
  @Test
  public void testDocPageShouldHaveListOfVersion() {
    open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    $("li.dropdown#version").shouldHave(text("1.0.6"));
  }
}
