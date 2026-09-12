import dev.detekt.gradle.plugin.getSupportedKotlinVersion

plugins {
    `java`
    `jacoco`
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.1.1"
    `conventions`
    `integration-test`
}

repositories {
    mavenCentral()
}

// detekt must run with the Kotlin version it was compiled against.
configurations.named("detekt") {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(getSupportedKotlinVersion())
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.apache.maven:maven-artifact:3.9.16")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.codehaus.janino:janino")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    annotationProcessor("org.springframework:spring-context-indexer")
}

defaultTasks("spotlessApply", "build")
