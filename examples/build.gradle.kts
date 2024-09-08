plugins {
    id("jsignal.common")
    id("jsignal.deps")
}

dependencies {
    implementation(project(":std"))
    implementation("ch.qos.logback:logback-classic:1.5.8")
}