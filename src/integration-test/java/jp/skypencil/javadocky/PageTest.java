package jp.skypencil.javadocky;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideDriver;
import java.util.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@ExtendWith(WebDriverCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PageTest {
  @LocalServerPort private int port;

  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testScreenShot(SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/page/jp.skypencil.guava/helper/1.0.1/");
  }

  /** Page page should have {@code <frameset>} to display javadoc. */
  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testPageShouldHaveFrameset(SelenideDriver driver) {
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
