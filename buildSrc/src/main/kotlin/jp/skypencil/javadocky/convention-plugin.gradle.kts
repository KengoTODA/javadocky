plugins {
  `java`
  `jacoco`
  id("org.sonarqube")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sonarqube {
  properties {
    property("sonar.coverage.jacoco.xmlReportPaths", tasks.jacocoTestReport.get().reports.xml.outputLocation.get())
  }
}
