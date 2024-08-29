plugins {
    id("java")
}

var exportedProjects = listOf(
    ":rx",
    ":ui",
    ":std"
)


tasks.register<Javadoc>("allJavadoc") {
    group = "documentation"
    source =
        files(*(exportedProjects.flatMap { project(it).java.sourceSets["main"].allJava }.toTypedArray())).asFileTree
    classpath = files(*(exportedProjects.flatMap { project(it).java.sourceSets["main"].compileClasspath }
        .toTypedArray())).asFileTree
    setDestinationDir(file("docs/public/javadoc"))
}

