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
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("io.usethesource:capsule:0.7.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.projectreactor:reactor-test:3.6.5")
}