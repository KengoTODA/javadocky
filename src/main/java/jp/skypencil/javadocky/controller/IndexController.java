package jp.skypencil.javadocky.controller;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Controller
class IndexController {
  @Bean
  public RouterFunction<ServerResponse> routeIndex() {
    return route(
        GET("/"),
        req ->
            ok().body(
                    Mono.just(new ClassPathResource("static/index.html")),
                    ClassPathResource.class));
  }
}
