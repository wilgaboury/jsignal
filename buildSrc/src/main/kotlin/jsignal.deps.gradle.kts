plugins {
    id("jsignal.common")
}

// need to separate out deps from common because rx has to shade dependencies

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}