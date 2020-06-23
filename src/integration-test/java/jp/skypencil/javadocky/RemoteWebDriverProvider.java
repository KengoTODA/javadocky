package jp.skypencil.javadocky;

import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

class RemoteWebDriverProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return Stream.of("firefox", "chrome")
        .map(browserName -> new Condition(browserName, context.getDisplayName()))
        .map(this::buildCapabilities)
        .map(this::init)
        .map(this::wrapDriver)
        .map(Arguments::of);
  }

  private Capabilities buildCapabilities(Condition condition) {
    DesiredCapabilities capabilities = new DesiredCapabilities();

    String sauceUserName = System.getenv("SAUCE_USERNAME");
    String sauceAccessKey = System.getenv("SAUCE_ACCESS_KEY");
    String sauceTunnelId = System.getenv("SAUCE_TUNNEL_ID");
    capabilities.setCapability("username", sauceUserName);
    capabilities.setCapability("accessKey", sauceAccessKey);
    capabilities.setCapability("tunnelIdentifier", sauceTunnelId);

    capabilities.setCapability("browserName", condition.browserName);
    capabilities.setCapability("console", true);
    capabilities.setCapability("name", condition.testName);
    capabilities.setCapability("network", true);
    capabilities.setCapability("platformName", condition.platformName);

    return capabilities;
  }

  private RemoteWebDriver init(Capabilities capabilities) {
    try {
      URL hub = new URL("https://ondemand.saucelabs.com/wd/hub");
      return new RemoteWebDriver(hub, capabilities);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to construct the URL of WebDriver hub.", e);
    }
  }

  private SelenideDriver wrapDriver(RemoteWebDriver webDriver) {
    return new SelenideDriver(new SelenideConfig(), webDriver, null);
  }

  static final class Condition {
    final String browserName;
    final String testName;
    final String platformName;

    Condition(String browserName, String testName) {
      this.browserName = browserName;
      this.testName = testName;
      this.platformName = "Windows 10";
    }
  }
}
