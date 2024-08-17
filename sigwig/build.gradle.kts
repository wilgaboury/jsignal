plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://releases.usethesource.io/maven")
    }
}

dependencies {
    api(project(":sigui"))
    api("com.google.guava:guava:32.1.3-jre")

    implementation("org.slf4j:slf4j-api:2.0.16")
}