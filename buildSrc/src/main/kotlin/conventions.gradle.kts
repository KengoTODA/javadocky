import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.google.cloud.tools.jib.gradle.JibTask
import de.undercouch.gradle.tasks.download.Download
import net.ltgt.gradle.errorprone.errorprone
import org.sonarqube.gradle.SonarQubeTask

plugins {
    `application`
    `jacoco`
    id("com.diffplug.spotless")
    id("de.undercouch.download")
    id("net.ltgt.errorprone")
    id("org.sonarqube")
    id("com.google.cloud.tools.jib")
    id("com.github.johnrengelman.shadow")
}

val jibExtraDirectory = "build/jib-agents"
val newRelicAgentPath = "newrelic"
val jacocoTestReport = tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

val downloadNewrelic by tasks.registering(Download::class) {
    src("https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip")
    dest(file("$buildDir"))
}

val unzipNewrelic by tasks.registering(Copy::class) {
    dependsOn(downloadNewrelic)
    from(zipTree(file("$buildDir/newrelic-java.zip")))
    into("$jibExtraDirectory/$newRelicAgentPath")
}

tasks {
    build {
        dependsOn(unzipNewrelic)
    }
    check {
        dependsOn(jacocoTestReport)
    }
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    withType<JavaCompile> {
        options.release.set(17)
    }
    withType<SonarQubeTask> {
        dependsOn(jacocoTestReport)
    }
    withType<JibTask> {
        dependsOn(unzipNewrelic)
    }
    withType<ShadowJar> {
        // https://github.com/spring-projects/spring-boot/issues/1828#issue-47834157
        mergeServiceFiles()
        append("META-INF/spring.handlers")
        append("META-INF/spring.schemas")
        append("META-INF/spring.tooling")
        transform(
            PropertiesFileTransformer().apply {
                paths = listOf("META-INF/spring.factories")
                mergeStrategy = "append"
            }
        )
    }
}

configure<JavaApplication> {
    mainClass.set("jp.skypencil.javadocky.JavadockyApplication")
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.11.0")
    errorprone("jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
        exclude("org.junit.jupiter")
        exclude("org.junit.vintage")
    }
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
        indentWithSpaces()
    }
}

jib {
    extraDirectories {
        paths {
            path {
                setFrom("$jibExtraDirectory/$newRelicAgentPath/newrelic")
                into = "/$newRelicAgentPath"
            }
        }
    }
    container {
        jvmFlags = listOf(
            "-javaagent:/$newRelicAgentPath/newrelic.jar",
        )
    }
    to {
        image = "ghcr.io/kengotoda/javadocky"
    }
}
