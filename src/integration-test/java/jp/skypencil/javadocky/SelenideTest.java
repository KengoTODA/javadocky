package jp.skypencil.javadocky;

import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.seljup.SeleniumExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SeleniumExtension.class)
abstract class SelenideTest {
  @LocalServerPort private int port;

  @BeforeEach
  void setBaseUrl() {
    Configuration.baseUrl = "http://localhost:" + port + "/";
  }
}
