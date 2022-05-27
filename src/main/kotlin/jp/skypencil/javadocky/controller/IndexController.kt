package jp.skypencil.javadocky.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import reactor.core.publisher.Mono

@Controller
internal class IndexController {
    @GetMapping(path = ["/"])
    @ResponseBody
    fun index(): Mono<ClassPathResource> {
        return Mono.just(ClassPathResource("static/index.html"))
    }
}
