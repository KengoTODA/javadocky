package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;

import com.codeborne.selenide.SelenideDriver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@ExtendWith(WebDriverCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocTest {
  @LocalServerPort private int port;

  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testScreenShot(SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
  }

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testDocPageShouldHaveIframe(SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    driver.$(By.tagName("iframe")).should(exist);
  }

  /** Doc page should have dropdown list to select {@code artifactId}. */
  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testDocPageShouldHaveListOfArtifactId(SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    driver.$("li.dropdown#artifact-id").shouldHave(text("helper"));
  }

  /** Doc page should have dropdown list to select {@code version}. */
  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testDocPageShouldHaveListOfVersion(SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/doc/jp.skypencil.guava/helper/");
    driver.$("li.dropdown#version").shouldHave(text("1.2.0"));
  }
}
