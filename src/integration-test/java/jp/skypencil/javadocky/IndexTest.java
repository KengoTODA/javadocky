package jp.skypencil.javadocky;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.element;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IndexTest {
  @LocalServerPort private int port;

  @Rule public BrowserStack browserStack = new BrowserStack();

  /** Doc page should have {@code <iframe>} to display {@code index.html}. */
  @Test
  public void testTitleExplainsServiceName() {
    open("http://localhost:" + port + "/");
    element(By.tagName("h1")).shouldHave(text("Javadocky"));
  }
}
