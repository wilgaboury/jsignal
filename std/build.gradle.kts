plugins {
    id("jsignal.publish")
    id("jsignal.deps")
}

dependencies {
    annotationProcessor(project(":prop"))
    compileOnly(project(":prop"))

    api(project(":ui"))
    api(project(":rx"))
    api("com.google.guava:guava:33.3.0-jre") // needed for MediaType
}