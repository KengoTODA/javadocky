plugins { id("com.gradle.develocity") version "4.5.0" }

rootProject.name = "javadocky"

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}
