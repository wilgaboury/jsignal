plugins {
    id("java")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jsignal"))
}