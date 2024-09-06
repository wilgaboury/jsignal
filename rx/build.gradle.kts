plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

tasks.named("jar") {
    enabled = false
}

dependencies {
    shadow("org.slf4j:slf4j-api:2.0.16")
    shadow("org.jetbrains:annotations:24.0.0")

    implementation("io.usethesource:capsule:0.7.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        named<MavenPublication>("mavenCentral") {
            shadow.component(this)
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}