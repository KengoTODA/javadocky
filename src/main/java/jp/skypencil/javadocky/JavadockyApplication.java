package jp.skypencil.javadocky;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class JavadockyApplication {
    private static final String STORAGE_DIR = "storage";
    /**
     * Name of directory to store downloaded javadoc.jar file.
     */
    private static final String JAVADOC_DIR = "javadoc";

    public static void main(String[] args) {
        SpringApplication.run(JavadockyApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routeIndex(DocumentController requestHandler) {
        return route(GET("/"),
                req -> ok().body(Mono.just(new ClassPathResource("static/index.html")), ClassPathResource.class));
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
