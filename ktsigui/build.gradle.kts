plugins {
    id("java-library")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":sigui"))
    api("com.google.guava:guava:32.1.3-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}