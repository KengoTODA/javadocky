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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class JavadockyApplication {
    private static final String URL_PATTERN = "/doc/{groupId}/{artifactId}/{version}/**";

    public static void main(String[] args) {
        SpringApplication.run(JavadockyApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routeIndex(RequestHandler requestHandler) {
        return route(GET("/"),
                req -> ok().body(Mono.just(new ClassPathResource("static/index.html")), ClassPathResource.class));
    }

    @Bean
    public RouterFunction<ServerResponse> routes(RequestHandler requestHandler) {
        return route(GET(URL_PATTERN),
                req -> buildResponse(req, requestHandler));
    }

    @Bean
    public Storage localStorage() {
        Path home = Paths.get(System.getProperty("user.home"), ".javadocky");
        home.toFile().mkdirs();
        log.info("Making storage at {}", home.toFile().getAbsolutePath());
        return new LocalStorage(home);
    }

    private Mono<ServerResponse> buildResponse(ServerRequest req, RequestHandler requestHandler) {
        String groupId = req.pathVariable("groupId");
        String artifactId = req.pathVariable("artifactId");
        String version = req.pathVariable("version");
        String path = findFilePath(req);
        if (path == null || path.isEmpty()) {
            path = "index.html";
        }

        return requestHandler.response(groupId, artifactId, version, path);
    }

    /**
     * @see <a href=
     *      "https://stackoverflow.com/questions/3686808/spring-3-requestmapping-get-path-value">related
     *      SO post</a>
     */
    private String findFilePath(ServerRequest req) {
        return new AntPathMatcher().extractPathWithinPattern(URL_PATTERN, req.path());
    }
}
