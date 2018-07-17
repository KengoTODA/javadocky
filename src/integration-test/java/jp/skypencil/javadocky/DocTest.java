package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.getElement;
import static com.codeborne.selenide.Selenide.open;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

import org.junit.Before;
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

  @Before
  public void config() {
    assumeThat(
        "path of chromedriver is set to 'webdriver.chrome.driver'",
        System.getProperty("webdriver.chrome.driver"),
        is(notNullValue()));
    System.setProperty("selenide.browser", "Chrome");
  }

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  public void testDocPageShouldHaveIframe() {
    open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    getElement(By.tagName("iframe")).should(exist);
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
    $("li.dropdown#version").shouldHave(text("1.0.1"));
  }
}
