

plugins {
    `java`
    `jacoco`
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.7.0"
    `conventions`
    `integration-test`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.apache.maven:maven-artifact:3.8.5")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.codehaus.janino:janino")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

defaultTasks("spotlessApply", "build")
