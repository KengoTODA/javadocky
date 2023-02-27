// refs https://docs.gradle.org/current/userguide/java_testing.html

plugins {
    `java-base`
    `kotlin`
    id("org.gradle.test-retry")
}

val integrationTest by sourceSets.creating {
    runtimeClasspath += sourceSets["main"].output.classesDirs
}

dependencies {
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "integrationTestImplementation"("io.github.bonigarcia:selenium-jupiter:4.1.0")
    "integrationTestImplementation"("com.codeborne:selenide:6.5.1")
    "integrationTestImplementation"("io.percy:percy-java-selenium:1.0.0")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
        exclude("org.junit.jupiter")
        exclude("org.junit.vintage")
    }
    "integrationTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    useJUnitPlatform()
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath

    mustRunAfter(tasks.test)

    retry {
        failOnPassedAfterRetry.set(true)
        maxFailures.set(10)
        maxRetries.set(3)
    }
}

tasks.check {
    dependsOn(integrationTestTask)
}
