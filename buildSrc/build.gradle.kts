import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.8.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.7.2")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.4.0.2513")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    implementation("org.gradle:test-retry-gradle-plugin:1.4.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.21.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
        indentWithSpaces()
    }
}
