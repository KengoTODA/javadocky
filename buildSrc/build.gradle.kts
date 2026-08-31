import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "8.10.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.10.0")
    implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:5.1.1")
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:7.4.0.8496")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.6.1")
    implementation("org.gradle.test-retry:org.gradle.test-retry.gradle.plugin:1.6.5")
    implementation("dev.detekt:dev.detekt.gradle.plugin:2.0.0-alpha.6")
}

tasks {
    withType<JavaCompile> {
        options.release.set(25)
    }
    withType<KotlinJvmCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
        leadingTabsToSpaces()
    }
}
