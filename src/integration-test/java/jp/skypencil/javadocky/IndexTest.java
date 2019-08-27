package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.text;

import com.codeborne.selenide.Browsers;
import com.codeborne.selenide.SelenideDriver;
import io.github.bonigarcia.seljup.SelenideConfiguration;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class IndexTest extends SelenideTest {
  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  void testTitleExplainsServiceName(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    driver.open("/");
    driver.$(By.tagName("h1")).shouldHave(text("Javadocky"));
  }
}
