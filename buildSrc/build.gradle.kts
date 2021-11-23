plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
}
