package jp.skypencil.javadocky;

import static com.codeborne.selenide.Selenide.element;
import static com.codeborne.selenide.Selenide.open;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import com.codeborne.selenide.Selenide;
import java.util.Objects;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PageTest {
  @LocalServerPort private int port;

  @Before
  public void config() {
    assumeThat(
        "path of chromedriver is set to 'webdriver.chrome.driver'",
        System.getProperty("webdriver.chrome.driver"),
        is(notNullValue()));
    System.setProperty("selenide.browser", "Chrome");
  }

  /** Page page should have {@code <frameset>} to display javadoc. */
  @Test
  public void testPageShouldHaveFrameset() {
    open("http://localhost:" + port + "/page/jp.skypencil.guava/helper/1.0.1/");
    assertTrue(element(By.tagName("frameset")).exists());
  }

  /** Page page should support {@code latest} version, which redirects to specific version. */
  @Test
  @Ignore("Not sure how to test redirect with Selenium/Selenide")
  public void testLatestPageRedirectsToSpecificVersion() {
    open("http://localhost:" + port + "/page/jp.skypencil.guava/helper/latest/");
    Selenide.Wait()
        .until(
            driver ->
                Objects.equals(
                    driver.getCurrentUrl(),
                    "http://localhost:" + port + "/page/jp.skypencil.guava/helper/1.0.1/"));
  }
}
