plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.functionaljava:functionaljava:5.0")
    implementation("org.slf4j:slf4j-api:2.0.12")

    implementation("org.jetbrains:annotations:24.0.0")
}