plugins {
    id("java")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jsignal"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}