plugins {
    id("java")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation(project(":jsigwig"))
    implementation(project(":ksignal"))
    implementation(project(":ksigui"))
    implementation(project(":ksigwig"))
}