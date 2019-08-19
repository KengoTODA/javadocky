package jp.skypencil.javadocky;

import com.browserstack.local.Local;
import com.codeborne.selenide.WebDriverRunner;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @see https://www.browserstack.com/automate/selenide
 * @see https://www.browserstack.com/automate/junit
 */
class BrowserStack implements TestRule {
  private RemoteWebDriver webDriver;

  @Override
  public Statement apply(Statement base, Description description) {
    Dotenv dotenv = Dotenv.load();
    String userName = dotenv.get("BROWSERSTACK_USERNAME");
    String accessKey = dotenv.get("BROWSERSTACK_ACCESS_KEY");
    String id = dotenv.get("BROWSERSTACK_LOCAL_IDENTIFIER");
    try {
      URL url =
          new URL(
              String.format(
                  "https://%s:%s@%s/wd/hub", userName, accessKey, "hub-cloud.browserstack.com"));
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Local local = new Local();
          try {
            local.start(Collections.singletonMap("key", accessKey));
            webDriver = new RemoteWebDriver(url, createCapabilities(id));
            WebDriverRunner.setWebDriver(webDriver);
            try {
              base.evaluate();
            } finally {
              webDriver.quit();
            }
          } finally {
            local.stop();
          }
        }
      };
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  RemoteWebDriver getWebDriver() {
    return webDriver;
  }

  private Capabilities createCapabilities(String id) {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability("project", "Javadocky");
    capabilities.setCapability("browserstack.debug", false);
    capabilities.setCapability("browserstack.local", true);
    capabilities.setCapability("browserstack.localIdentifier", id);
    return capabilities;
  }
}
