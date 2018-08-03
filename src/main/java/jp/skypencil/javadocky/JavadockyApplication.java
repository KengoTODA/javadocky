package jp.skypencil.javadocky;

import java.nio.file.Path;
import java.nio.file.Paths;
import jp.skypencil.javadocky.repository.ArtifactRepository;
import jp.skypencil.javadocky.repository.LocalStorage;
import jp.skypencil.javadocky.repository.LocalStorageArtifactRepository;
import jp.skypencil.javadocky.repository.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JavadockyApplication {
  private static final String STORAGE_DIR = "storage";
  /** Name of directory to store downloaded javadoc.jar file. */
  private static final String JAVADOC_DIR = "javadoc";

  private final Logger log = LoggerFactory.getLogger(getClass());

  public static void main(String[] args) {
    SpringApplication.run(JavadockyApplication.class, args);
  }

  @Bean
  public Storage localStorage() {
    Path home = Paths.get(System.getProperty("user.home"), ".javadocky", STORAGE_DIR);
    home.toFile().mkdirs();
    log.info("Making storage at {}", home.toFile().getAbsolutePath());
    return new LocalStorage(home);
  }

  @Bean
  public ArtifactRepository artifactRepository() {
    Path home = Paths.get(System.getProperty("user.home"), ".javadocky", JAVADOC_DIR);
    home.toFile().mkdirs();
    log.info("Making storage at {}", home.toFile().getAbsolutePath());
    return new LocalStorageArtifactRepository(home);
  }

  @Bean
  public JavadocDownloader javadocDownloader() {
    Path home = Paths.get(System.getProperty("user.home"), ".javadocky", JAVADOC_DIR);
    home.toFile().mkdirs();
    log.info("Making javadoc storage at {}", home.toFile().getAbsolutePath());
    return new JavadocDownloader(home);
  }
}
