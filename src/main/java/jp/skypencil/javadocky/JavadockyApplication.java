package jp.skypencil.javadocky;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import jp.skypencil.javadocky.repository.ArtifactRepository;
import jp.skypencil.javadocky.repository.LocalStorage;
import jp.skypencil.javadocky.repository.LocalStorageArtifactRepository;
import jp.skypencil.javadocky.repository.Storage;
import jp.skypencil.javadocky.service.JavadocDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JavadockyApplication {
  private static final String USER_HOME = "user.home";
  private static final String JAVADOCKY_ROOT = ".javadocky";

  private static final String STORAGE_DIR = "storage";
  /** Name of directory to store downloaded javadoc.jar file. */
  private static final String JAVADOC_DIR = "javadoc";

  private final Logger log = LoggerFactory.getLogger(getClass());

  public static void main(String[] args) {
    final boolean isAppCds = Arrays.asList(args).contains("--appcds");
    String port = System.getenv("PORT");
    SpringApplication app = new SpringApplication(JavadockyApplication.class);
    if (port != null) {
      // for Heroku, respect the given PORT environment variable
      app.setDefaultProperties(Collections.singletonMap("server.port", port));
    }

    ConfigurableApplicationContext ctx = app.run(args);

    // TODO consider the best timing to stop the process
    if (isAppCds) {
      System.err.println("Beans construction complete, so going to exit the process");
      ctx.close();
    }
  }

  @Bean
  public Storage localStorage() {
    Path home = Paths.get(System.getProperty(USER_HOME), JAVADOCKY_ROOT, STORAGE_DIR);
    home.toFile().mkdirs();
    log.info("Making storage at {}", home.toFile().getAbsolutePath());
    return new LocalStorage(home);
  }

  @Bean
  public ArtifactRepository artifactRepository() {
    Path home = Paths.get(System.getProperty(USER_HOME), JAVADOCKY_ROOT, JAVADOC_DIR);
    home.toFile().mkdirs();
    log.info("Making storage at {}", home.toFile().getAbsolutePath());
    return new LocalStorageArtifactRepository(home);
  }

  @Bean
  public JavadocDownloader javadocDownloader(
      @Value("${javadocky.maven.repository}") String repoURL) {
    Path home = Paths.get(System.getProperty(USER_HOME), JAVADOCKY_ROOT, JAVADOC_DIR);
    home.toFile().mkdirs();
    log.info("Making javadoc storage at {}", home.toFile().getAbsolutePath());
    return new JavadocDownloader(home, repoURL);
  }
}
