plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://releases.usethesource.io/maven")
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation(project(":sigwig"))
}