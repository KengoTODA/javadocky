package jp.skypencil.javadocky;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class JavadockyApplication {
    private static final String URL_PATTERN = "/doc/{groupId}/{artifactId}/{version}/**";

    public static void main(String[] args) {
        SpringApplication.run(JavadockyApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route(GET(URL_PATTERN),
                req -> ok().body(Flux.just(buildResponse(req)), String.class));
    }

    private String buildResponse(ServerRequest req) {
        String groupId = req.pathVariable("groupId");
        String artifactId = req.pathVariable("artifactId");
        String version = req.pathVariable("version");
        String path = findFilePath(req);
        if (path == null || path.isEmpty()) {
            return String.format("%s:%s:%s", groupId, artifactId, version);
        } else {
            return String.format("%s:%s:%s/%s", groupId, artifactId, version, path);
        }
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
