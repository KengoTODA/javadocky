package javadocky;

import static com.codeborne.selenide.Selenide.getElement;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

public class PageTest {

    @Before
    public void config() {
        assumeThat("path of chromedriver is set to 'webdriver.chrome.driver'",
                System.getProperty("webdriver.chrome.driver"), is(notNullValue()));
        System.setProperty("selenide.browser", "Chrome");
    }

    /**
     * Page page should have {@code <frameset>} to display javadoc.
     */
    @Test
    public void testPagePageShouldHaveFrameset() {
        open("http://localhost:8080/page/jp.skypencil.guava/helper/1.0.1/");
        assertTrue(getElement(By.tagName("frameset")).exists());
    }

    /**
     * Page page should support {@code latest} version, which redirects to specific version.
     */
    @Test
    public void testDocPageShouldHaveListOfVersion() {
        open("http://localhost:8080/page/jp.skypencil.guava/helper/latest/");
        assertThat(getWebDriver().getCurrentUrl(), is("http://localhost:8080/page/jp.skypencil.guava/helper/1.0.1/"));
    }
}
