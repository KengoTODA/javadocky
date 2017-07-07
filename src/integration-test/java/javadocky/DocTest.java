package javadocky;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.getElement;
import static com.codeborne.selenide.Selenide.open;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

public class DocTest {

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
    public void testDocPageShouldHaveIframe() {
        open("http://localhost:8080/doc/jp.skypencil.guava/helper/1.0.1/");
        assertTrue(getElement(By.tagName("iframe")).exists());
    }

    /**
     * Doc page should have dropdown list to select {@code artifactId}.
     */
    @Test
    public void testDocPageShouldHaveListOfArtifactId() {
        open("http://localhost:8080/doc/jp.skypencil.guava/helper/1.0.1/");
        $("div.dropdown#artifact-id").shouldHave(text("helper"));
    }

    /**
     * Doc page should have dropdown list to select {@code version}.
     */
    @Test
    public void testDocPageShouldHaveListOfVersion() {
        open("http://localhost:8080/doc/jp.skypencil.guava/helper/1.0.1/");
        $("div.dropdown#version").shouldHave(text("1.0.1"));
    }
}
