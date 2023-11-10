plugins {
    id("java")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ktsignal"))
    implementation(project(":ktsigui"))
    implementation(project(":sigwig"))
}