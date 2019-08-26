package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.element;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.Browsers;
import com.codeborne.selenide.SelenideDriver;
import io.github.bonigarcia.seljup.SelenideConfiguration;
import io.github.bonigarcia.seljup.SeleniumExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SeleniumExtension.class)
class DocTest {
  @LocalServerPort private int port;

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  void testDocPageShouldHaveIframe(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    open("http://172.17.0.1:" + port + "/doc/jp.skypencil.guava/helper/");
    element(By.tagName("iframe")).should(exist);
  }

  /** Doc page should have dropdown list to select {@code artifactId}. */
  @Test
  void testDocPageShouldHaveListOfArtifactId(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    open("http://172.17.0.1:" + port + "/doc/jp.skypencil.guava/helper/");
    $("li.dropdown#artifact-id").shouldHave(text("helper"));
  }

  /** Doc page should have dropdown list to select {@code version}. */
  @Test
  void testDocPageShouldHaveListOfVersion(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    open("http://172.17.0.1:" + port + "/doc/jp.skypencil.guava/helper/");
    $("li.dropdown#version").shouldHave(text("1.2.0"));
  }
}
