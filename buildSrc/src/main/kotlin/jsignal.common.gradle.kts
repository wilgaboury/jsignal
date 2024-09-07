plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    signing
}

group = "org.jsignal"
version = "0.0.4"

repositories {
    mavenCentral()
    maven {
        url = uri("https://releases.usethesource.io/maven")
    }
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}