plugins {
    id("java-library")
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":ktsigui"))

    api("com.google.guava:guava:32.1.3-jre")
}