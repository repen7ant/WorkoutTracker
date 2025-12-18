plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
    id("jacoco")
    id("com.github.spotbugs") version "6.0.17"
    id("checkstyle")
}

group = "app.tracker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("app.tracker")
    mainClass.set("app.tracker.MainApplication")
}

javafx {
    version = "17.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
    testImplementation("com.h2database:h2:2.2.224")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.+")
}

jacoco {
    toolVersion = "0.8.12"
}

checkstyle {
    toolVersion = "10.12.0"
    config = resources.text.fromUri("https://raw.githubusercontent.com/checkstyle/checkstyle/master/src/main/resources/sun_checks.xml")
}

spotbugs {
    toolVersion.set("4.8.3")
    ignoreFailures.set(false)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
    ignoreFailures.set(true)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
        outputLocation.set(layout.buildDirectory.file("reports/spotbugs/${name}.html"))
    }
    reports.create("xml") {
        required.set(false)
    }
    ignoreFailures = true
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<Checkstyle>().configureEach {
    source("src/main/java")
    configFile = checkstyle.configFile
    ignoreFailures = true
    reports {
        html.required.set(true)
        xml.required.set(false)
    }
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}


