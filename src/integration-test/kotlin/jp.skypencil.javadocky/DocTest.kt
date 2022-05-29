package jp.skypencil.javadocky

import com.codeborne.selenide.Browsers
import com.codeborne.selenide.Condition.exist
import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.SelenideDriver
import io.github.bonigarcia.seljup.SelenideConfiguration
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.percy.selenium.Percy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@ExtendWith(SeleniumJupiter::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocTest {
    @LocalServerPort
    private val port = 0

    @Test
    fun testScreenShot(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/doc/jp.skypencil.guava/helper/")
        val percy = Percy(driver.getWebDriver())
        percy.snapshot("Doc Page")
    }

    /** Doc page should have `<iframe>` to display `index.html`.  */
    @Test
    fun testDocPageShouldHaveIframe(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/doc/jp.skypencil.guava/helper/")
        driver.`$`(By.tagName("iframe")).should(com.codeborne.selenide.Condition.exist)
    }

    /** Doc page should have dropdown list to select `artifactId`.  */
    @Test
    fun testDocPageShouldHaveListOfArtifactId(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/doc/jp.skypencil.guava/helper/")
        driver.`$`("li.dropdown#artifact-id").shouldHave(com.codeborne.selenide.Condition.text("helper"))
    }

    /** Doc page should have dropdown list to select `version`.  */
    @Test
    fun testDocPageShouldHaveListOfVersion(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/doc/jp.skypencil.guava/helper/")
        driver.`$`("li.dropdown#version").shouldHave(com.codeborne.selenide.Condition.text("1.2.0"))
    }
}
