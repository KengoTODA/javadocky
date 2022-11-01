plugins { id("com.gradle.enterprise") version "3.11.3" }

rootProject.name = "javadocky"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
