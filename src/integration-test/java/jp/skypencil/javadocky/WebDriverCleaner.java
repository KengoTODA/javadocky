package jp.skypencil.javadocky;

import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * To prevent test execution from a timeout, invoke {@code WebDriver.quit()} after test execution.
 *
 * @see <a
 *     href="https://www.lambdatest.com/support/docs/java-with-selenium-running-java-automation-scripts-on-lambdatest-selenium-grid/">LambdaTest
 *     document</a>
 */
class WebDriverCleaner implements TestWatcher {
  @Override
  public void testSuccessful(ExtensionContext context) {
    closeDriver(true);
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    closeDriver(false);
  }

  @Override
  public void testAborted(ExtensionContext context, Throwable cause) {
    closeDriver(true);
  }

  private void closeDriver(boolean status) {
    WebDriver driver = WebDriverRunner.getWebDriver();
    if (driver == null) {
      return;
    }

    ((JavascriptExecutor) driver).executeScript("sauce:job-result=" + status ? "passed" : "failed");
    driver.quit();
  }
}
