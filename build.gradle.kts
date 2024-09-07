plugins {
    id("jsignal.common")
}

val projectNames = subprojects.map { it.name }

tasks.register<Javadoc>("allJavadoc") {
    group = "documentation"
    source =
        files(*(projectNames.flatMap { project(it).java.sourceSets["main"].allJava }.toTypedArray())).asFileTree
    classpath = files(*(projectNames.flatMap { project(it).java.sourceSets["main"].compileClasspath }
        .toTypedArray())).asFileTree
    setDestinationDir(file("docs/public/javadoc"))
}