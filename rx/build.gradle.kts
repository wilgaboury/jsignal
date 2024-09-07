plugins {
    id("jsignal.publish")
    id("jsignal.test-deps")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation("io.usethesource:capsule:0.7.1")

    shadow("org.slf4j:slf4j-api:2.0.16")
    shadow("jakarta.annotation:jakarta.annotation-api:3.0.0")

    testImplementation("org.slf4j:slf4j-api:2.0.16")
    testImplementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
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