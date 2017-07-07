package javadocky;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.getElement;
import static com.codeborne.selenide.Selenide.open;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

public class IndexTest {

    @Before
    public void config() {
        assumeThat("path of chromedriver is set to 'webdriver.chrome.driver'",
                System.getProperty("webdriver.chrome.driver"), is(notNullValue()));
        System.setProperty("selenide.browser", "Chrome");
    }

    /**
     * Doc page should have {@code <iframe>} to display {@code index.html}.
     */
    @Test
    public void testTitleExplainsServiceName() {
        open("http://localhost:8080/");
        getElement(By.tagName("h1")).shouldHave(text("Javadocky"));
    }
}
