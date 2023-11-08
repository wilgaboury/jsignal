plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jsignal"))
    implementation(project(":sigui"))
    implementation(project(":sigwig"))
}