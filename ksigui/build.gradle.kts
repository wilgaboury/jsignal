plugins {
    id("java-library")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":jsigui"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}