import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import dev.detekt.gradle.Detekt
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.file.DuplicatesStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.sonarqube.gradle.SonarTask

plugins {
    `application`
    `jacoco`
    `kotlin`
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
    id("org.sonarqube")
    id("com.gradleup.shadow")
    id("dev.detekt")
}

val jacocoTestReport = tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks {
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
        options.release.set(25)
    }
    withType<KotlinJvmCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
    }
    withType<SonarTask> {
        dependsOn(jacocoTestReport)
    }
    withType<ShadowJar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        // https://github.com/spring-projects/spring-boot/issues/1828#issue-47834157
        mergeServiceFiles()
        append("META-INF/spring.handlers")
        append("META-INF/spring.schemas")
        append("META-INF/spring.tooling")
        append("META-INF/spring/aot.factories")
        append(
            "META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports"
        )
        append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
        transform(
            PropertiesFileTransformer(project.objects).apply {
                paths.set(listOf("META-INF/spring.factories"))
                mergeStrategy.set(PropertiesFileTransformer.MergeStrategy.Append)
            }
        )
    }
    withType<Detekt> {
        reports {
            html.required.set(true)
            sarif.required.set(true)
        }
    }
}

configure<JavaApplication> {
    mainClass.set("jp.skypencil.javadocky.JavadockyApplication")
}

val koTestVersion = "6.2.2"
dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    errorprone("jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.29")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
    testImplementation("io.kotest:kotest-property:$koTestVersion")
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
        languageVersion.set(JavaLanguageVersion.of(25))
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
        leadingTabsToSpaces()
    }
    kotlin {
        ktlint()
        leadingTabsToSpaces()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
}
