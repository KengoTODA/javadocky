package jp.skypencil.javadocky

import com.codeborne.selenide.Browsers
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.SelenideDriver
import io.github.bonigarcia.seljup.SelenideConfiguration
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.percy.selenium.Percy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.util.Objects

@ExtendWith(SeleniumJupiter::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PageTest {
    @LocalServerPort
    private val port = 0

    @Test
    fun testScreenShot(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/page/jp.skypencil.guava/helper/1.0.1/")
        val percy = Percy(driver.getWebDriver())
        percy.snapshot("Page for an artifact")
    }

    /** Page page should have `<frameset>` to display javadoc.  */
    @Test
    fun testPageShouldHaveFrameset(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/page/jp.skypencil.guava/helper/1.0.1/")
        assertTrue(driver.`$`(By.tagName("frameset")).exists())
    }

    /** Page page should support `latest` version, which redirects to specific version.  */
    @Test
    @Disabled("Not sure how to test redirect with Selenium/Selenide")
    fun testLatestPageRedirectsToSpecificVersion() {
        Selenide.open("http://localhost:$port/page/jp.skypencil.guava/helper/latest/")
        Selenide.Wait()
            .until<Boolean>({ driver: WebDriver ->
                Objects.equals(
                    driver.getCurrentUrl(),
                    "http://localhost:$port/page/jp.skypencil.guava/helper/1.0.1/"
                )
            }
            )
    }
}
