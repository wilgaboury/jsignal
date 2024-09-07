dependencies {
    api(project(":prop"))
    api(project(":ui"))
    api(project(":rx"))
    api("com.google.guava:guava:33.3.0-jre")

    annotationProcessor(project(":prop"))
}