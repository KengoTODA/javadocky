import java.io.ByteArrayOutputStream

plugins {
    `java`
    `jacoco`
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.6.3"
    `conventions`
    `integration-test`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.apache.maven:maven-artifact:3.8.4")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.codehaus.janino:janino")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

val hash: String = ByteArrayOutputStream().use { outputStream ->
    project.exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = outputStream
    }
    outputStream.toString().trim()
}

tasks {
    bootJar {
        manifest {
            attributes("Git-Hash" to hash)
        }
    }
}

defaultTasks("spotlessApply", "build")
