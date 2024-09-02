plugins {
    id("java")
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.23.1")
//    implementation("org.jsignal:std:0.0.1")
    implementation(project(":std"))
}