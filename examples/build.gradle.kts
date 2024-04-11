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
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation(project(":sigwig"))
}