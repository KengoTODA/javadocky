package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;

import com.codeborne.selenide.Browsers;
import com.codeborne.selenide.SelenideDriver;
import io.github.bonigarcia.seljup.SelenideConfiguration;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class DocTest extends SelenideTest {
  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  void testDocPageShouldHaveIframe(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    driver.open("/doc/jp.skypencil.guava/helper/");
    driver.$(By.tagName("iframe")).should(exist);
  }

  /** Doc page should have dropdown list to select {@code artifactId}. */
  @Test
  void testDocPageShouldHaveListOfArtifactId(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    driver.open("/doc/jp.skypencil.guava/helper/");
    driver.$("li.dropdown#artifact-id").shouldHave(text("helper"));
  }

  /** Doc page should have dropdown list to select {@code version}. */
  @Test
  void testDocPageShouldHaveListOfVersion(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    driver.open("/doc/jp.skypencil.guava/helper/");
    driver.$("li.dropdown#version").shouldHave(text("1.2.0"));
  }
}
