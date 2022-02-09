import de.undercouch.gradle.tasks.download.Download
import net.ltgt.gradle.errorprone.errorprone
import org.sonarqube.gradle.SonarQubeTask

plugins {
    `java`
    `jacoco`
    id("com.diffplug.spotless")
    id("de.undercouch.download")
    id("net.ltgt.errorprone")
    id("org.sonarqube")
}

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
    into("$buildDir")
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
    withType<JavaCompile> {
        options.release.set(17)
    }
    withType<SonarQubeTask> {
        dependsOn(jacocoTestReport)
    }
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