plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":sigui"))

    api("com.google.guava:guava:32.1.3-jre")
}