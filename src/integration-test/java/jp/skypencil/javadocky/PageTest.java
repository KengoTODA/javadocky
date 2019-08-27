package jp.skypencil.javadocky;

import static com.codeborne.selenide.Browsers.CHROME;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideDriver;
import io.github.bonigarcia.seljup.SelenideConfiguration;
import java.util.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class PageTest extends SelenideTest {
  /** Page page should have {@code <frameset>} to display javadoc. */
  @Test
  void testPageShouldHaveFrameset(
      @SelenideConfiguration(browser = CHROME, headless = true) SelenideDriver driver) {
    driver.open("/page/jp.skypencil.guava/helper/1.0.1/");
    assertTrue(driver.$(By.tagName("frameset")).exists());
  }

  /** Page page should support {@code latest} version, which redirects to specific version. */
  @Test
  @Disabled("Not sure how to test redirect with Selenium/Selenide")
  void testLatestPageRedirectsToSpecificVersion() {
    open("/page/jp.skypencil.guava/helper/latest/");
    Selenide.Wait()
        .until(
            driver ->
                Objects.equals(
                    driver.getCurrentUrl(),
                    Configuration.baseUrl + "/page/jp.skypencil.guava/helper/1.0.1/"));
  }
}
