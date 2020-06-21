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

    capabilities.setCapability("browserName", condition.browserName);
    capabilities.setCapability("console", true);
    capabilities.setCapability("name", condition.testName);
    capabilities.setCapability("network", true);
    capabilities.setCapability("platformName", condition.platformName);
    capabilities.setCapability("timezone", "UTC+08:00");
    capabilities.setCapability("tunnel", true);
    capabilities.setCapability("visual", true);

    return capabilities;
  }

  private RemoteWebDriver init(Capabilities capabilities) {
    String username = System.getenv("LT_USERNAME");
    String accessKey = System.getenv("LT_ACCESS_KEY");

    try {
      URL hub =
          new URL(String.format("https://%s:%s@hub.lambdatest.com/wd/hub", username, accessKey));
      return new RemoteWebDriver(hub, capabilities);
    } catch (MalformedURLException e) {
      throw new RuntimeException(
          "Failed to construct the URL of WebDriver hub, "
              + "check LT_USERNAME and LT_ACCESS_KEY environment variables.",
          e);
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
