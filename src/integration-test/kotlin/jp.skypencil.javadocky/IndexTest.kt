package jp.skypencil.javadocky

import com.codeborne.selenide.Browsers
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
internal class IndexTest {
    @LocalServerPort
    private val port = 0

    /** Doc page should have `<iframe>` to display `index.html`.  */
    @Test
    fun testTitleExplainsServiceName(
        @SelenideConfiguration(browser = Browsers.CHROME, headless = true) driver: SelenideDriver
    ) {
        driver.open("http://localhost:$port/")
        driver.`$`(By.tagName("h1")).shouldHave(text("Javadocky"))
        val percy = Percy(driver.getWebDriver())
        percy.snapshot("Index Page")
    }
}
