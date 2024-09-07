plugins {
    id("jsignal.test-deps")
}

// need to separate out deps from common because rx has to shade dependencies

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
}