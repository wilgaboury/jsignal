plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

tasks.named("jar") {
    enabled = false
}

dependencies {
    shadow("org.slf4j:slf4j-api:2.0.16")
    shadow("jakarta.annotation:jakarta.annotation-api:3.0.0")

    implementation("io.usethesource:capsule:0.7.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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