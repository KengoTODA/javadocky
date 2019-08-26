package jp.skypencil.javadocky;

import static com.codeborne.selenide.Browsers.CHROME;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideDriver;
import io.github.bonigarcia.seljup.SelenideConfiguration;
import io.github.bonigarcia.seljup.SeleniumExtension;
import java.util.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SeleniumExtension.class)
class PageTest {
  @LocalServerPort private int port;

  /** Page page should have {@code <frameset>} to display javadoc. */
  @Test
  void testPageShouldHaveFrameset(
      @SelenideConfiguration(browser = CHROME, headless = true) SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/page/jp.skypencil.guava/helper/1.0.1/");
    assertTrue(driver.$(By.tagName("frameset")).exists());
  }

  /** Page page should support {@code latest} version, which redirects to specific version. */
  @Test
  @Disabled("Not sure how to test redirect with Selenium/Selenide")
  void testLatestPageRedirectsToSpecificVersion() {
    open("http://localhost:" + port + "/page/jp.skypencil.guava/helper/latest/");
    Selenide.Wait()
        .until(
            driver ->
                Objects.equals(
                    driver.getCurrentUrl(),
                    "http://localhost:" + port + "/page/jp.skypencil.guava/helper/1.0.1/"));
  }
}
