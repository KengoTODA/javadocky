package jp.skypencil.javadocky;

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
class IndexTest {
  @LocalServerPort private int port;

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @ParameterizedTest
  @ArgumentsSource(RemoteWebDriverProvider.class)
  void testTitleExplainsServiceName(SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/");
    driver.$(By.tagName("h1")).shouldHave(text("Javadocky"));
  }
}
