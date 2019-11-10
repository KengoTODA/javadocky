package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.text;

import com.codeborne.selenide.Browsers;
import com.codeborne.selenide.SelenideDriver;
import io.github.bonigarcia.seljup.SelenideConfiguration;
import io.github.bonigarcia.seljup.SeleniumExtension;
import io.percy.selenium.Percy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@ExtendWith(SeleniumExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IndexTest {
  @LocalServerPort private int port;

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  void testTitleExplainsServiceName(
      @SelenideConfiguration(browser = Browsers.CHROME, headless = true) SelenideDriver driver) {
    driver.open("http://localhost:" + port + "/");
    driver.$(By.tagName("h1")).shouldHave(text("Javadocky"));
    Percy percy = new Percy(driver);
    percy.snapshot("Index Page");
  }
}
